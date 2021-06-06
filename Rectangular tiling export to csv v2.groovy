//Exports only a CSV file with two columns for the X and Y coordinates of the UPPER LEFT CORNER of the tile, in pixels
//Pixel values are based on the upper left corner of the whole image.

createTiles = true

double frameWidth = 1320/4
double frameHeight = 1040/4
double overlap = 50
baseDirectory = "f:/Data"

clearDetections()
//Potentially store tiles as they are created
newTiles = []

//Store XY coordinates in an array
xy = []
//Check all annotations. Use .findAll{expression} to select a subset
annotations = getAnnotationObjects()
imageName = getCurrentServer().getFile().getName()
//Ensure the folder to store the csv exists
tilePath = buildFilePath(baseDirectory, "Tiles csv")
mkdirs(tilePath)
//CSV will be only two columns with the following header
String header="x_pos,y_pos";

annotations.each{a->
    roiA = a.getROI()
    //generate a bounding box of the whole annotation to create tiles within
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
///////////////////////
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
