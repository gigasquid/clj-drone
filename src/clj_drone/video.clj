(ns clj-drone.video
  (:import (java.net Socket))
  (:import javax.imageio.ImageIO)
  (:import (java.io FileOutputStream DataOutputStream File))
  (:import javax.swing.JFrame
           javax.swing.JPanel
           java.awt.FlowLayout)
  (:require [clj-drone.navdata :refer [bytes-to-int get-short get-int]]
            [clj-drone.decode :refer :all]
            [clj-drone.opencv :refer :all]
            [clj-drone.core :refer :all]))

 ;This can records raw video to a file called stream.m4v
;To convert to video use
;ffmpeg -f h264 -an -i vid.h264 stream.m4v



;;;;;

(def window (JFrame. "test"))
(def view (JPanel. ))
(def stream (atom true))
(def header-size 76)
(def video-agent (agent 0))
(def save-video (atom false))
(def vsocket (atom nil))
(def opencv (atom false))

(defn configure-save-video [b]
  (reset! save-video b))

(defn configure-opencv [b]
  (reset! opencv b))

(defn setup-viewer []
  (def window (JFrame. "test"))
  (def view (JPanel. ))
  (doto window
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setBounds 30 30 (if @opencv 320 640) (if @opencv 180 360)))
  (.add (.getContentPane window) view)
  (.setVisible window true)
  (def g (.getGraphics view)))

(defn update-image [bi]
  (do
    (.drawImage g bi 10 10 view)))

;;wakes it up

(defn init-video-stream [host]
  (do
    (let [vs (Socket. host 5555)]
      (.setSoTimeout vs 5000)
      ;wakes up the socket so that it will continue to stream data
      (.write (DataOutputStream. (.getOutputStream vs)) (byte-array (map byte [1 0 0 0])))
      (reset! vsocket vs))))

(defn read-from-input [size]
  (let [bv (byte-array size)]
    (.read (.getInputStream @vsocket) bv)
    bv))

(defn read-header []
  (read-from-input header-size))

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
  (read-from-input size))

(defn write-payload [video out]
  (.write out video))

(defn save-image [bi]
  (ImageIO/write bi "png" (File. "opencvin.png")))

(defn display-frame [video]
  (try
    (let [buff-img (convert-frame video)]
      (def my-img buff-img)
      (update-image
       (if @opencv
         (process-and-return-image buff-img)
         buff-img)))
    (catch Exception e (println (str "Error displaying frame - skipping " e)))))

(defn read-frame [host out]
  (try
    (let [vheader (read-header)]
     (if (> (count vheader) -1)
       (if (= "PaVE" (read-signature vheader))
         (do
           (let [vpayload (read-payload (payload-size vheader))]
             (when out
               (write-payload vpayload out))
             (display-frame vpayload)))
         (do (println "not a pave")))
       (do (println "disconnected")
           (.close @vsocket)
           (init-video-stream host)
                                        ;need to wait a bit after reconnecting 
           (Thread/sleep 600))))
    (catch Exception e (println (str "Problem reading frame - skipping " e)))))

(defn stream-video [_ host out]
  (while @stream (do
                   (read-frame host out))))


(defn end-video []
  (reset! stream false))


(defn init-video [host]
  (init-decoder)
  (setup-viewer)
  (init-video-stream host))

(defn start-video [host]
  (do
    (reset! stream true)
    (Thread/sleep 40)
    ;wait for the first frame
    (send video-agent stream-video host (when @save-video
                                          (FileOutputStream. "vid.h264")))))


;;Debugging stuffs

;(configure-opencv true)
;(init-video "192.168.1.1")
;(start-video "192.168.1.1")





;; ( convert-buffer-image-to-mat my-img :color)
;; (double (/ 1000 15))
;(drone-initialize)
;(init-video (drone-ip :default))
;(start-video (drone-ip :default))

;; (drone :video-frame-rate-15)
;(init-opencv)
;(init-decoder)
;(setup-viewer)
; (init-video-stream "192.168.1.1")
;(start-video "192.168.1.1")



;; (time (read-frame "192.168.1.1" nil))
;; (double (/ 1000 15))

;; my-img
;; (.drawImage g2 my-img 10 10 view1)
;; (.drawImage (.getGraphics view1) my-img 10 10 view1)
;; (update-image)
;; ;; (time (read-frame "192.168.1.1" nil))
 ;(agent-errors video-agent)

;; (restart-agent display-agent1  0)
;(end-video)






