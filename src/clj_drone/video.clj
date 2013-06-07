(ns clj-drone.video
  (:import (java.net DatagramPacket DatagramSocket InetAddress Socket))
  (:import (java.io ByteArrayInputStream InputStreamReader FileOutputStream BufferedOutputStream))
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]))



(def video-socket (DatagramSocket. ))
(def stream (atom true))
(def video-output (FileOutputStream. "test.out"))

;;wakes it up

(defn init-video-stream []
  (drone-initialize)
  (def skt (Socket. default-drone-ip 5555))
  (init-streaming-navdata video-socket drone-host 5555)
  (def video-output (FileOutputStream. "test.out")))


(defn read-video []
  (def bvideo (byte-array (* 100 1024)))
  (.read (.getInputStream skt) bvideo))
;; now read the data

(defn read-signature [in]
  (String. (byte-array (map #(nth in %1) [0 1 2 3]))))

(defn get-uint8 [ba offset]
  (bytes-to-int ba offset 1))

(defn read-header [in]
  (let [version (get-uint8 bvideo 4)
        codec (get-uint8 bvideo 5)
        header-size (get-short bvideo 6)
        payload-size (get-int bvideo 8)
        encoded-width (get-short bvideo 12)
        encoded-height (get-short bvideo 14)
        display-width (get-short bvideo 16)
        display-height (get-short bvideo 18)
        frame-number (get-int bvideo 20)]
    {:version version
     :codec codec
     :header-size header-size
     :payload-size payload-size
     :encoded-width encoded-width
     :encoded-height encoded-height
     :display-width display-width
     :display-height display-height
     :frame-number frame-number}))

(defn header-size [in]
  (:header-size (read-header in)))

(defn payload-size [in]
  (:payload-size (read-header in)))

(defn write-payload [in]
  (when (= (read-signature in) "PaVE")
   (do
     (.write video-output
             (byte-array
              (map byte
                   (reduce #(conj %1 (nth bvideo (+ (header-size in) %2))) [] (range 0 (payload-size in)))))))))


(defn stream-video []
  (when @stream
    (do
      (read-video)
      (write-payload bvideo)
      (recur))))


;(init-video-stream)
;(read-video)
;(write-payload bvideo)
;(read-signature bvideo)
;(payload-size bvideo)

;(future (stream-video))






