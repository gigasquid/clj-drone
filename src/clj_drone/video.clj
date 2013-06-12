(ns clj-drone.video
  (:import (java.net Socket))
  (:import (java.io FileOutputStream DataOutputStream))
  (:require [clj-drone.navdata :refer [bytes-to-int get-short get-int]]
            [clj-drone.core :refer :all]))

 ;This records raw video to a file called stream.m4v
;To convert to video use
;ffmpeg -f h264 -an -i vid.h264 stream.m4v


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
  (read-from-input size @vsocket))

(defn write-payload [video out]
  (.write out video))

(defn read-frame [host out]
  (if (> (read-header) -1)
    (if (= "PaVE" (read-signature bvideo))
      (do
        (read-payload (payload-size bvideo))
        (write-payload bvideo out))
      (do (println "not a pave")))
    (do (println "disconnected")
        (.close @vsocket)
        (init-video-stream host)
        (Thread/sleep 600)
        ;(reset! stream false)
        )))

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


;;try future with drone commands? - nope

 ;; (drone-initialize)
;; (init-video-stream "192.168.1.1")
;; (start-video "192.168.1.1")
;; (drone :flat-trim)
;; (end-video)
;; (read-frame "192.168.1.1")

;; (read-header)
;; (read-signature bvideo)
;;  (read-payload (payload-size bvideo))
;; (write-payload bvideo out)

;; bvideo
;; (write-payload bvideo)
;; video-agent
;; (agent-errors video-agent)














