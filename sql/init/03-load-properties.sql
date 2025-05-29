LOAD DATA INFILE '/var/lib/mysql-files/cleaned_airbnb_data.csv'
INTO TABLE Properties
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(ID, Host_ID, Price, Room_type, Person_capacity, Bedrooms, Center_distance, Metro_distance, City);
