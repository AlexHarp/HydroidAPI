# create common folder
mkdir staging

# build hydroid api
mvn -DskipTests=true clean package
cp target/hydroid.war staging/
