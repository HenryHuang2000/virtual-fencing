# virtual-fencing backend

The java backend server for the virtual fencing project. The virtual fencing project aims to automatically detect when users have violated home quarantine under the covid-19 restrictions. An app installed on the quarantined indvidual's phone will periodically send API calls to this backend server if the individual has not violated quarantine. This server tracks and records activity, notifying authorities if it has not received notifications from a phone for 15 minutes.

To run locally: ./mvnw clean install -DskipTests && docker-compose up --build -d
