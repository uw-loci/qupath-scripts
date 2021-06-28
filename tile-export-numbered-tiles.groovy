//Goal is to create tile images that are #.tiff and also create a text file with the coordinates.
//#.tiff files are used in the Grid/collection stitcher or BigStitcher to determine order, or to match with a TileConfiguration.txt

double frameWidth = 3000
double frameHeight = 3000
//This is in PERCENT, do not put 0.1 for 10%
double overlapPercent = 0

baseDirectory = path = buildFilePath(PROJECT_BASE_DIR, 'TileExport')

clearDetections()
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
String header="dim = 2";
path = buildFilePath(baseDirectory, "Tiles csv", "TileConfiguration.txt")
index = 0
new File(path).withWriter { fw ->
    fw.writeLine(header)
    //Make sure everything being sent is a child and part of the current annotation.

    annotations.each{a->
        roiA = a.getROI()
        //generate a bounding box to create tiles within
        bBoxX = a.getROI().getBoundsX()
        bBoxY = a.getROI().getBoundsY()
        bBoxH = a.getROI().getBoundsHeight()
        bBoxW = a.getROI().getBoundsWidth()
        y = bBoxY
       
        while (y< bBoxY+bBoxH){
            x = bBoxX
            while (x < bBoxX+bBoxW){
                
                xy << [x,y]
                //create the rectangle object for reference
                def roi = new RectangleROI(x,y,frameWidth,frameHeight, ImagePlane.getDefaultPlane())
                if(roiA.getGeometry().intersects(roi.getGeometry())){
                    newTiles << PathObjects.createDetectionObject(roi)
                
                 //Create a text file with the coordinates for stitching
                    String line = index +".tiff; ; ("+x+", "+y+")" 
                    fw.writeLine(line)
                    pathToImage = buildFilePath(baseDirectory, "Image tiles", index+".tiff")
                    requestROI = RegionRequest.createInstance(server.getPath(), 1, roi)
                    writeImageRegion(server, requestROI, pathToImage)
                    index++
                }
                x = x+frameWidth-overlapPercent/100*frameWidth
                
                
            }
            y = y+frameHeight-overlapPercent/100*frameHeight
            
        }
    }


}

//Comment out to avoid visual tiles.
addObjects(newTiles)
resolveHierarchy()

print " "
print "Output saved in  folder at " + tilePath
print "done"


import qupath.lib.regions.ImagePlane
import qupath.lib.roi.RectangleROI;
