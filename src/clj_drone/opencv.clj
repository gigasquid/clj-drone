(ns clj-drone.opencv
  (:import
    org.opencv.core.Core
    org.opencv.core.Mat
    org.opencv.core.MatOfRect
    org.opencv.core.Point
    org.opencv.core.Rect
    org.opencv.core.Scalar
    org.opencv.highgui.Highgui
    org.opencv.objdetect.CascadeClassifier
    java.awt.image.BufferedImage
    javax.imageio.ImageIO
    java.io.File))

(def face-detections (atom []))

(defn create-classifier []
  (CascadeClassifier.
     (.getPath (clojure.java.io/resource
                "lbpcascade_frontalface.xml" ))))

(def front-face-classifier (create-classifier))

(defn load-image [filename]
  (Highgui/imread filename))

(defn detect-faces! [classifier image]
  (.detectMultiScale classifier image @face-detections))

(defn draw-bounding-boxes!
  [image]
  (doall (map (fn [rect]
                (Core/rectangle image
                        (Point. (.x rect) (.y rect))
                        (Point. (+ (.x rect) (.width rect))
                                (+ (.y rect) (.height rect)))
                        (Scalar. 0 255 0)))
              (.toArray @face-detections)))
  (Highgui/imwrite "faceDetections.png" image)
  (ImageIO/read (File. "faceDetections.png")))


(defn process-and-save-image! [filename]
  (let [image (load-image filename)]
    (detect-faces! (create-classifier) image)
    (draw-bounding-boxes! image)))

(defn process-and-return-image [filename]
  (let [image (load-image filename)]
    (do
      (reset! face-detections (MatOfRect.))
      (detect-faces! front-face-classifier image)
      (draw-bounding-boxes! image))))

;(process-and-return-image "opencvin.png")

(defn init-opencv []
  (clojure.lang.RT/loadLibrary Core/NATIVE_LIBRARY_NAME))







