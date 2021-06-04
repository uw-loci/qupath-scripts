//@MichaelSNelson
//04062021
/*
  Deletes all detections
  Tiles all annotations with detection objects, and then exports the centroids of those tiles to a CSV
  All detections are then exported as .tif files
  Set frameWidth, frameHeight and overlap
  Set the baseDirectory, unless you have an F:/Data folder!
*/
createTiles = true

double frameWidth = 1320/2
double frameHeight = 1040/2
double overlap = 50
baseDirectory = "f:/Data"

clearDetections()
//Potentially store tiles as they are created
newTiles = []

//Store XY coordinates in an array
xy = []
//Check all annotations. Use .findAll{expression} to select a subset
annotations = getAnnotationObjects()
server = getCurrentServer()
imageName = getCurrentServer().getFile().getName()
//Ensure the folder to store the csv exists
tilePath = buildFilePath(baseDirectory, "Tiles csv")
mkdirs(tilePath)
imagePath = buildFilePath(baseDirectory, "Image tiles")
mkdirs(imagePath)
//CSV will be only two columns with the following header
String header="x_pos,y_pos";

annotations.each{a->
    roiA = a.getROI()
    //generate a bounding box to create tiles within
    bBoxX = a.getROI().getBoundsX()
    bBoxY = a.getROI().getBoundsY()
    bBoxH = a.getROI().getBoundsHeight()
    bBoxW = a.getROI().getBoundsWidth()
    x = bBoxX
   
    while (x< bBoxX+bBoxW){
        y = bBoxY
        while (y < bBoxY+bBoxH){
            if(createTiles==true){createATile(x, y, frameWidth, frameHeight, overlap, roiA)}

            y = y+frameHeight-overlap
            xy << [x,y]
        }
        x = x+frameWidth-overlap
    }
}
path = buildFilePath(baseDirectory, "Tiles csv", imageName+".csv")
new File(path).withWriter { fw ->
    fw.writeLine(header)
    //Make sure everything being sent is a child and part of the current annotation.
    
    xy.each{
        String line = it[0] as String +","+it[1] as String
        fw.writeLine(line)
    }
}

//Comment out to avoid visual tiles.
if(createTiles==true){
    addObjects(newTiles)
    resolveHierarchy()
}
getDetectionObjects().each{
    roi = it.getROI()
    x = roi.getCentroidX()
    y = roi.getCentroidY()
    path = buildFilePath(baseDirectory, "Image tiles", "["+x+","+y+"]"+"_"+imageName+".tif")
    requestROI = RegionRequest.createInstance(server.getPath(), 1, roi)
    writeImageRegion(server, requestROI, path)
    
}
print " "
print "Output saved in  folder at " + tilePath


print "done"

def createATile(x,y,width,height, overlap, roiA) {
    def roi = new RectangleROI(x,y,width,height, ImagePlane.getDefaultPlane())
    if(roiA.getGeometry().intersects(roi.getGeometry())){
        newTiles << PathObjects.createDetectionObject(roi)
    }
}

import qupath.lib.regions.ImagePlane
import qupath.lib.roi.RectangleROI;
