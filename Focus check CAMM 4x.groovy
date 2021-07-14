setImageType('BRIGHTFIELD_H_E');
setColorDeconvolutionStains('{"Name" : "CAMM 4x", "Stain 1" : "Hematoxylin", "Values 1" : "0.65565 0.71107 0.25396", "Stain 2" : "Eosin", "Values 2" : "0.21015 0.84065 0.49914", "Background" : " 245 245 246"}');
createAnnotationsFromPixelClassifier("Tissue", 500000.0, 50000.0, "DELETE_EXISTING", "SELECT_NEW")
addPixelClassifierMeasurements("FocusCheck", "FocusCheck")
