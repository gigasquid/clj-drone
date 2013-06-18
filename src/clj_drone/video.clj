(ns clj-drone.video
  (:import (java.net Socket))
  (:import javax.imageio.ImageIO)
  (:import (java.io FileOutputStream DataOutputStream File))
  (:import javax.swing.JFrame
           javax.swing.JPanel)
  (:require [clj-drone.navdata :refer [bytes-to-int get-short get-int]]
            [clj-drone.decode :refer :all]
            [clj-drone.opencv :refer :all]))

 ;This can records raw video to a file called stream.m4v
;To convert to video use
;ffmpeg -f h264 -an -i vid.h264 stream.m4v



;;;;;

(def window (JFrame. "test"))
(def view (JPanel. ))
(def stream (atom true))
(def header-size 68)
(def video-agent (agent 0))
(def vsocket (atom nil))
(def save-video (atom false))

(defn configure-save-video [b]
  (reset! save-video b))

(defn setup-viewer []
  (def window (JFrame. "test"))
  (def view (JPanel. ))
  (doto window
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setBounds 30 30 640 360))
  (.add (.getContentPane window) view)
  (.setVisible window true)
  (def g (.getGraphics view)))

(defn update-image [bi]
  (do
    (.drawImage g bi 10 10 view)))


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

(defn save-image [bi]
    (ImageIO/write bi "png" (File. "opencvin.png")))

(defn display-frame [video]
  (try
    (let [buff-img (convert-frame video)]
      (do
        ;(update-image buff-img)
        (save-image buff-img)
        (update-image (process-and-return-image "opencvin.png"))))
    (catch Exception e (println (str "Error displaying frame - skipping " e)))))

(defn read-frame [host out]
  (if (> (read-header) -1)
    (if (= "PaVE" (read-signature bvideo))
      (do
        (read-payload (payload-size bvideo))
        (when out
          (write-payload bvideo out))
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
    (Thread/sleep 40)
    ;catch up to preset
    (send video-agent stream-video host (when @save-video
                                          (FileOutputStream. "vid.h264")))))

(init-decoder)
(init-opencv)
(setup-viewer)
(init-video-stream "192.168.1.1")
(read-frame "192.168.1.1" nil)
(start-video "192.168.1.1")
;(agent-errors video-agent)
;(restart-agent video-agent 0)
(end-video)




















