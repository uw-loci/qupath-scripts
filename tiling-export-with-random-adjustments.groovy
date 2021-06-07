createTiles = false

double frameWidth = 1320/2
double frameHeight = 1040/2
//This is in PERCENT, do not put 0.1 for 10%
double overlapPercent = 10
// padding turns 1 into 001, or 10 into 010. Increase if you need more than 999 tiles in a direction.
padding = 3
baseDirectory = "f:/Data"

//clearDetections()
//Potentially store tiles as they are created
newTiles = []

//Store XY coordinates in an array
xy = []
//Check all annotations. Use .findAll{expression} to select a subset
annotations = getAnnotationObjects()
server = getCurrentServer()
imageName = GeneralTools.getNameWithoutExtension(getCurrentServer().getMetadata().getName())
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
            if(createTiles==true){createATile(x, y, frameWidth, frameHeight, roiA)}
            fudge = ((Math.random()-0.5)*10).round(1)
            //print fudge
            y = y+frameHeight-overlapPercent/100*frameHeight+fudge
            xy << [x,y]
        }
        fudge = ((Math.random()-0.5)*10).round(1)
        //print fudge
        x = x+frameWidth-overlapPercent/100*frameWidth + fudge
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
    print "detection"
    roi = it.getROI()
    int x = ((roi.getCentroidX()-bBoxX-frameWidth/2)/(frameWidth-overlapPercent/100*frameWidth)+1).round(1)
    int y = ((roi.getCentroidY()-bBoxY-frameHeight/2)/(frameHeight-overlapPercent/100*frameHeight)+1).round(1)
    xString = x.toString()
    yString = y.toString()
    print x
    print y
    path = buildFilePath(baseDirectory, "Image tiles", "["+xString.padLeft(padding, '0')+","+yString.padLeft(padding, '0')+"]"+"_"+imageName+".tif")
    requestROI = RegionRequest.createInstance(server.getPath(), 1, roi)
    writeImageRegion(server, requestROI, path)
    
}
print " "
print "Output saved in  folder at " + tilePath


print "done"

def createATile(x,y,width,height, roiA) {
    def roi = new RectangleROI(x,y,width,height, ImagePlane.getDefaultPlane())
    if(roiA.getGeometry().intersects(roi.getGeometry())){
        newTiles << PathObjects.createDetectionObject(roi)
    }
}

import qupath.lib.regions.ImagePlane
import qupath.lib.roi.RectangleROI;