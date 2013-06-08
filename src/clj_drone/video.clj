(ns clj-drone.video
  (:import (java.net DatagramPacket DatagramSocket InetAddress Socket))
  (:import (java.io ByteArrayInputStream InputStreamReader FileOutputStream BufferedOutputStream))
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]))

;ffmpeg -f h264 -an -i vid.h264 stream.m4v

(def video-socket (DatagramSocket. ))
(def stream (atom true))
(def header-size 68)
(declare skt)
(def video-agent (agent 0))

;;wakes it up

(defn init-video-stream []
  (drone-initialize)
  (def skt (Socket. default-drone-ip 5555))
  (init-streaming-navdata video-socket drone-host 5555)
  (def video-output (FileOutputStream. "test.out")))


(defn read-from-input [size]
  (def bvideo (byte-array size))
  (.read (.getInputStream skt) bvideo))

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
        frame-number (get-int in 20)]
    {:version version
     :codec codec
     :header-size header-size
     :payload-size payload-size
     :encoded-width encoded-width
     :encoded-height encoded-height
     :display-width display-width
     :display-height display-height
     :frame-number frame-number}))

(defn payload-size [in]
  (:payload-size (get-header in)))

(defn read-payload [size]
  (read-from-input size))

(defn read-frame []
  (when (> (read-header) -1)
   (do
     (println (str  "payload sig " (read-signature bvideo)))
     (println (str  "payload size is " (payload-size bvideo)))
     (if (= "PaVE" (read-signature bvideo))
       (do
         (println "writing payload")
         (read-payload (payload-size bvideo))
         (write-payload bvideo))
       (println "skipping")))))

(defn write-payload [in]
  (.write video-output in))

(defn stream-video [_]
  (def video-output (FileOutputStream. "vid.h264"))
  (if (read-frame)
    (do
      (recur nil))))



;; This works
; (init-video-stream)
;;   (read-header)
 ;;  (read-signature bvideo)
 ;;  (payload-size bvideo)
 ;; (read-payload (payload-size bvideo))
 ;; (write-payload bvideo)

;;(read-frame)

;(write-payload bvideo)


;; (stream-video nil)
;(send-off video-agent stream-video)


;(agent-errors video-agent)



;; (read-frame)
;(restart-agent video-agent 0)










