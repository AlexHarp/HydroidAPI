# create common folder
mkdir staging
sed -i 's/__HYDROID_POSTGRES_PWD__/'"$HYDROID_POSTGRES_PWD"'/g' src/main/java/resources/application.properties
# build hydroid api
mvn clean package
cp target/hydroid.jar staging/
cp google-vision.json staging/