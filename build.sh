# create common folder
mkdir staging

# build hydroid api
mvn clean package
cp target/hydroid.war staging/
