(ns clj-drone.video
  (:import (java.net Socket))
  (:import (java.io FileOutputStream DataOutputStream))
  (:require [clj-drone.navdata :refer [bytes-to-int get-short get-int]]))

 ;This records raw video to a file called stream.m4v
;To convert to video use
;ffmpeg -f h264 -an -i vid.h264 stream.m4v


(def stream (atom true))
(def header-size 68)
(declare vid-skt)
(def video-agent (agent 0))
(def video-output (FileOutputStream. "vid.h264"))

;;wakes it up

(defn init-video-stream [host]
  (def vid-skt (Socket. host 5555))
  (.setSoTimeout vid-skt 1000)
  (def dos (DataOutputStream. (.getOutputStream vid-skt)))
  (do (.write dos (byte-array (map byte [1 0 0 0])))))

(defn read-from-input [size]
  (def bvideo (byte-array size))
  (.read (.getInputStream vid-skt) bvideo))

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

(defn write-payload [in]
  (.write video-output in))

(defn read-frame []
  (when (> (read-header) -1)
    (if (= "PaVE" (read-signature bvideo))
      (do
        (read-payload (payload-size bvideo))
        (write-payload bvideo)))))

(defn stream-video [_]
  (while @stream (do
                   (read-frame)
                   (Thread/sleep 30))))

(defn end-video []
  (reset! stream false)
  (.close video-output))


(defn start-video []
  (do
    (reset! stream true)
    (def video-output (FileOutputStream. "vid.h264"))
    (Thread/sleep 30)
    (send video-agent stream-video)))

;; (drone-initialize)
;; (init-video-stream drone-host)
 ;(start-video)
;(end-video)
;(read-frame)














