(ns clj-drone.video
  (:import (java.net DatagramPacket DatagramSocket InetAddress Socket))
  (:import (java.io ByteArrayInputStream InputStreamReader))
  (:require [clj-drone.core :refer :all]
            [clj-drone.navdata :refer :all]))



(def bvideo (byte-array 392))
(def video-socket (DatagramSocket. ))

;;wakes it up

(defn init-video-stream []
  (drone-initialize)
  (def skt (Socket. default-drone-ip 5555))
  (init-streaming-navdata video-socket drone-host 5555))


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

(init-video-stream)
(read-video)
(read-header bvideo)

(defn header-size [in]
  (:header-size (read-header in)))

(defn payload-size [in]
  (:payload-size (read-header in)))

(header-size bvideo)
(payload-size bvideo)



(get-uint8 bvideo 4);=>2  version
(get-uint8 bvideo 5)  ; 4=> codec
(get-short bvideo 6) ; => 68 header size
(get-int bvideo 8) ;=> 25784 payload size
(get-short bvideo 12) ;=>  640 encoded stream width
(get-short bvideo 14) ;=>  368 encoded stream width
(get-short bvideo 16) ;=>  640 display width
(get-short bvideo 18) ;=>  360 display height
(get-int bvideo 20) ;=> 1545 frame number


;;;this should be the start of the payload
(nth bvideo 68)
(nth bvideo 25784)

;; need a function to parse header and then the payload and write it
;; out to a file writer

;(init-video-stream)
;(read-video)

(nth bvideo 4)
(get-int bvideo (+ 0 16))
(read-string bvideo 16)

(defn read-version [in])

(read-signature bvideo)

(bytes-to-int bvideo 0 8)

