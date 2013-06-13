(ns example.opencv
  (:import
    org.opencv.core.Core
    org.opencv.core.Mat
    org.opencv.core.MatOfRect
    org.opencv.core.Point
    org.opencv.core.Rect
    org.opencv.core.Scalar
    org.opencv.highgui.Highgui
    org.opencv.objdetect.CascadeClassifier))

(def face-detections (atom []))

(defn create-classifier
  []
  (CascadeClassifier.
     (.getPath (clojure.java.io/resource
                  "lbpcascade_frontalface.xml" ))))

(defn load-image
  []
  (Highgui/imread
   (.getPath (clojure.java.io/resource "lena.png"))))

(defn detect-faces!
  [classifier image]
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
  (Highgui/imwrite "faceDetections.png" image))

(defn process-and-save-image!
  []
  (let [image (load-image)]
    (detect-faces! (create-classifier) image)
    (draw-bounding-boxes! image)))


(clojure.lang.RT/loadLibrary Core/NATIVE_LIBRARY_NAME)

(reset! face-detections (MatOfRect.))
(process-and-save-image!)




