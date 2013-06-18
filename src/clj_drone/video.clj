(ns clj-drone.video
  (:import (java.net Socket))
  (:import (java.io FileOutputStream DataOutputStream ByteArrayInputStream ByteArrayOutputStream FileInputStream DataInputStream))
  (:import javax.swing.JFrame
           javax.swing.JPanel
           javax.swing.ImageIcon
           javax.swing.JLabel
           java.awt.FlowLayout
           java.awt.Dimension
           java.awt.Container
           javax.imageio.ImageIO
           java.awt.image.BufferedImage
           )
  (:import com.twilight.h264.decoder.AVFrame
           com.twilight.h264.decoder.AVPacket
           com.twilight.h264.decoder.H264Decoder
           com.twilight.h264.decoder.MpegEncContext
           com.twilight.h264.player.FrameUtils
           java.util.Arrays)
  (:require [clj-drone.navdata :refer [bytes-to-int get-short get-int]]
            [me.raynes.conch :as conch]))

 ;This records raw video to a file called stream.m4v
;To convert to video use
                                        ;ffmpeg -f h264 -an -i
                                        ;vid.h264 stream.m4v

;;;
;;getting input data in the right form
;(def ba (byte-array 21490))
;(def filein (DataInputStream. (FileInputStream. "good.h264")))
;(.read filein ba)

;;;

(def first-frame (atom true))

(defn init-decoder []
  (do
    (def INBUF_SIZE 65535)
    (def inbuf-int (int-array (+ INBUF_SIZE MpegEncContext/FF_INPUT_BUFFER_PADDING_SIZE)))
    (def avpkt (AVPacket.))
    (.av_init_packet avpkt)

    (def codec (H264Decoder.))
    (if (nil? codec) (println "Codec not found"))
    (def c (MpegEncContext/avcodec_alloc_context))
    (def picture (AVFrame/avcodec_alloc_frame))


    (if (not (= 0 (bit-and (.capabilities codec) H264Decoder/CODEC_CAP_TRUNCATED)))
      (println "need to configure CODEC_FLAG_TRUNCATED"))
    (if (< (.avcodec_open c codec) 0)
      (println "Could not open codec"))))


(defn to-ba-int [b]
  (doall (for [i (range 0 (count b))]
     (aset-int inbuf-int i (bit-and 0xFF (nth b i))))))

(defn convert! [got-picture]
  (let [len (.avcodec_decode_video2 c picture got-picture avpkt)]
    len))

(defn get-image-icon [picture buffer]
      (let [image  (BufferedImage.
                    (.imageWidth picture) (.imageHeight picture) BufferedImage/TYPE_INT_RGB)]
        (do
          (.setRGB
           image 0 0 (.imageWidth picture) (.imageHeight picture) buffer 0 (.imageWidth picture))
          (ImageIcon. image))))

(defn convert-frame [b]
  (do
    (def got-picture (int-array [0]))
    (to-ba-int b)
    (set! (.size avpkt) (count b))
    (set! (.data_base avpkt) inbuf-int)
    (set! (.data_offset avpkt) 0)
    (if (> (convert! got-picture) 0)
      (if (first got-picture)
        (let [ picture (.displayPicture (.priv_data c))
              buffer-size (* (.imageHeight picture) (.imageWidth picture))
              buffer (int-array buffer-size)
              ]
          (do
            (FrameUtils/YUV2RGB picture buffer)
            (get-image-icon picture buffer)
            )
          )
        )
      (println "Could not decode frame"))))

;;;;;


(def frame (JFrame. "Drone view"))
(def view (JPanel. ))

(def lena (ImageIcon. "resources/lena.png"))
(def testimage (ImageIcon. "test.png"))

(defn view-image [icon]
  (do
    (def label (JLabel.))
    (.setIcon label icon)
    (.setLayout view (FlowLayout.))
    (.add view label)
    (def dimension (Dimension. (.getIconWidth icon) (.getIconHeight icon)))
    (.setPreferredSize view dimension)
    (.setMaximumSize view dimension)
    (.setMinimumSize view dimension)
    (def contentPane (.getContentPane frame))
    (.setLayout contentPane (FlowLayout.))
    (.add contentPane view)
    (.setDefaultCloseOperation frame JFrame/EXIT_ON_CLOSE)
    (.pack frame)
    (.setVisible frame true)))

(defn update-image [icon]
  (do
    (.setIcon label icon)))


(def stream (atom true))
(def header-size 68)
(def video-agent (agent 0))
(def vsocket (atom nil))

;;wakes it up

(defn init-video-stream [host]
  (do
    (reset! vsocket (Socket. host 5555))
    (.setSoTimeout @vsocket 5000)
    (def dos (DataOutputStream. (.getOutputStream @vsocket)))
    (.write dos (byte-array (map byte [1 0 0 0])))))

(defn read-from-input [size video-socket]
  (def bvideo (byte-array size))
  (.read (.getInputStream video-socket) bvideo))

(defn read-header []
  (read-from-input header-size @vsocket))

(defn read-signature [in]
  (String. (byte-array (map #(nth in %1) [0 1 2 3]))))

(defn get-uint8 [ba offset]
  (bytes-to-int ba offset 1))

(defn get-header [in]
  (let [version (get-uint8 in 4)
        codec (get-uint8 in 5)
        header-size (get-short in 6)
        payload-size (get-int in 8)
        encoded-width (get-short in 12)
        encoded-height (get-short in 14)
        display-width (get-short in 16)
        display-height (get-short in 18)
        frame-number (get-int in 20)
        timestamp (get-int in 24)
        total-chunks (get-uint8 in 28)
        chunk-index (get-uint8 in 29)
        frame-type (get-uint8 in 30)
        control (get-uint8 in 31)
        stream-byte-pos-lw (get-int in 32)
        stream-byte-pos-uw (get-int in 36)
        stream-id (get-short in 40)
        total-slices (get-uint8 in 42)
        slice-index (get-uint8 in 43)
        header1-size (get-uint8 in 44)
        header2-size (get-uint8 in 45)
        advertised-size (get-int in 48)]
    {:version version
     :codec codec
     :header-size header-size
     :payload-size payload-size
     :encoded-width encoded-width
     :encoded-height encoded-height
     :display-width display-width
     :display-height display-height
     :frame-number frame-number
     :timestamp timestamp
     :total-chunks total-chunks
     :chunk-index chunk-index
     :frame-type frame-type
     :control control
     :stream-byte-pos-lw stream-byte-pos-lw
     :stream-bypte-pos-uw stream-byte-pos-uw
     :stream-id stream-id
     :total-slices total-slices
     :slice-index slice-index
     :header1-size header1-size
     :header2-size header2-size
     :advertised-size advertised-size
     }))


(defn payload-size [in]
  (:payload-size (get-header in)))

(defn read-payload [size]
  (read-from-input size @vsocket))

(defn write-payload [video out]
  (.write out video))

(defn display-frame [video]
  (if @first-frame
    (do
      (reset! first-frame false)
      (view-image (convert-frame video)))
    (update-image (convert-frame video))))

(defn read-frame [host out]
  (if (> (read-header) -1)
    (if (= "PaVE" (read-signature bvideo))
      (do
        (read-payload (payload-size bvideo))
        (write-payload bvideo out)
        (display-frame bvideo)
        )
      (do (println "not a pave")))
    (do (println "disconnected")
        (.close @vsocket)
        (init-video-stream host)
        ;need to wait a bit after reconnecting 
        (Thread/sleep 600))))

(defn stream-video [_ host out]
  (while @stream (do
                   (read-frame host out)
                   (Thread/sleep 30))))

(defn end-video []
  (reset! stream false))


(defn start-video [host]
  (do
    (reset! stream true)
    (Thread/sleep 30)
    (send video-agent stream-video host (FileOutputStream. "vid.h264"))))



;(init-decoder)
;(init-video-stream "192.168.1.1")
;(start-video "192.168.1.1")
;(agent-errors video-agent)
;(end-video)


;(read-frame "192.168.1.1" (FileOutputStream. "next.h264"))
;bvideo
 ;(convert-frame bvideo)


;; (init-decoder)
;; (def my-first-decoded-png (convert-frame bvideo))
;; (view-image my-first-decoded-png)
;; (update-image my-first-decoded-png)





; (read-payload (payload-size bvideo))
; (write-payload bvideo  (FileOutputStream. "newframe.h264"))

;(display-frame bvideo)
;(nth bvideo 4)

;(display-frame bvideo)
;(start-video "192.168.1.1")
;(end-video)
;(nth bvideo 3)















