# create common folder
mkdir staging

# build hydroid api
mvn clean package
cp target/hydroid.jar staging/
cp google-vision.json staging/