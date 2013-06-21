(ns clj-drone.opencv
  (:import
   org.opencv.core.Core
   org.opencv.core.CvType
   org.opencv.core.Mat
   org.opencv.core.MatOfRect
   org.opencv.core.MatOfByte
   org.opencv.core.Point
   org.opencv.core.Rect
   org.opencv.core.Scalar
   org.opencv.highgui.Highgui
   org.opencv.objdetect.CascadeClassifier
   org.opencv.objdetect.Objdetect
   java.awt.image.BufferedImage
   java.awt.RenderingHints
   javax.imageio.ImageIO
   java.io.File
   java.io.ByteArrayInputStream
   org.opencv.core.Size))


(defn buf-to-mat [buf type]
  (let [itype (if (= type :gray)  CvType/CV_8UC1  CvType/CV_8UC3)
        img-b  (-> buf (.getRaster) (.getDataBuffer) (.getData))
        mat (Mat. (.getHeight buf) (.getWidth buf) itype)]
    (.put mat 0 0 img-b)
    mat))


(defn convert-buffer-image-to-mat [img type]
  (let [itype (if (= type :gray) BufferedImage/TYPE_BYTE_GRAY BufferedImage/TYPE_3BYTE_BGR)
        w (.getWidth img)
        h (.getHeight img)
        nw (/ w 2)
        nh (/ h 2)
        new-frame  (BufferedImage. nw nh itype)
        g (.getGraphics new-frame)]
    (doto g
      (.setRenderingHint RenderingHints/KEY_INTERPOLATION  RenderingHints/VALUE_INTERPOLATION_BILINEAR)
      (.drawImage img 0 0 nw nh 0 0 w h nil)
      (.dispose))
    (buf-to-mat new-frame type))
  )

(defn convert-mat-to-buffer-image [mat]
  (let [new-mat (MatOfByte.)]
    (Highgui/imencode ".png" mat new-mat)
    (ImageIO/read (ByteArrayInputStream. (.toArray new-mat)))))

(def face-detections (atom []))
(declare front-face-classifier)

(defn create-classifier []
  (CascadeClassifier.
     (.getPath (clojure.java.io/resource
                "haarcascade_frontalface_alt.xml" ))))

(defn load-image [filename]
  (Highgui/imread filename))

(defn detect-faces! [classifier image]
  (.detectMultiScale classifier
                     image
                     @face-detections
                     (double 1.2)
                     2
                     Objdetect/CASCADE_DO_CANNY_PRUNING
                     (Size. 80 80)
                     (Size. 180 180)))
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
  (convert-mat-to-buffer-image image))


(defn process-and-save-image! [filename]
  (let [image (load-image filename)]
    (detect-faces! (create-classifier) image)
    (draw-bounding-boxes! image)))

(defn process-and-return-image [imgbuf]
  (let [image (convert-buffer-image-to-mat imgbuf :gray)]
    (do
      (reset! face-detections (MatOfRect.))
      (detect-faces! front-face-classifier image)
      (draw-bounding-boxes! image))))

;(process-and-return-image "opencvin.png")

(defn init-opencv []
  (clojure.lang.RT/loadLibrary Core/NATIVE_LIBRARY_NAME)
  (def front-face-classifier (create-classifier)))

