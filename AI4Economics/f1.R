#
# Setting up some vectors
#
subject_name <- c("John Doe", "Jane Doe", "Steve Graves")
temperature <- c(98.1, 98.6, 101.4)
flu_status <- c(FALSE, FALSE, TRUE)

# Checking vectors values
temperature[2]
temperature[2:3]

# Filtering vectors
temperature[c(TRUE, TRUE, FALSE)]
fever <- temperature > 100
subject_name[fever]
subject_name[temperature>100]

#
# Setting up some factors
#
gender <- factor(c("MALE","FEMALE","MALE"))
gender

blood <- factor(c("O","AB","A"),
                levels = c("A","B","AB","O"))
blood

symptoms <- factor(c("SEVERE","MILD","MODERATE"),
                   levels = c("MILD","MODERATE","SEVERE"),
                   ordered = TRUE)
symptoms
symptoms > "MODERATE"

#
# Setting up some list
#
subject1 <- list(fullname = subject_name[1],
                 temperature = temperature[1],
                 flu_status = flu_status[1],
                 gender = gender[1],
                 blood = blood[1],
                 symptoms = symptoms[1])
subject1

# Selecting sublist (type list)
subject1[2]
subject1$temperature

# Selecting the the list component (native type i.e. numeric)
subject1[[2]]

# Selecting several components
subject1[c("temperature","flu_status")]

#
# Setting up some data frames
#
pt_data <- data.frame(subject_name,
                      temperature,
                      flu_status,
                      gender,
                      blood,
                      symptoms)
pt_data

# Filtering some components
pt_data$subject_name

pt_data[c("temperature","flu_status")]
pt_data[2:3]

pt_data[1, 2]
pt_data[c(1,3),c(2,4)]

pt_data[,1]
pt_data[2,]
pt_data[,]

pt_data[c(1,3),c("temperature","flu_status")]

# New columsn
pt_data$temp_c <- (pt_data$temperature-32) * (5/9)
pt_data[c("temperature","temp_c")]

#
# Matrix
# Important! column-major order: first column is loaded before the second one
#
m <- matrix(c(1,2,3,4),nrow=2)
m

m <- matrix(c(1,2,3,4,5,6),nrow=2)
m

m <- matrix(c(1,2,3,4,5,6),ncol=2)
m

#
# Saving and loading data structures
#
save(pt_data,file="f1.RData")
load ("f1.RData")

#
# Saving and loading single objects.
# It is used for saving and loading machine learning objects.
#
saveRDS(pt_data, file ="my_model.rds")
# Assigning to a different name
my_model <- readRDS("my_model.rds")
my_model

#
# Listing data structures in memory
#
ls()
# Releasing my_model data structure
rm(my_model)

# Releasing everything
# rm(list=ls())

# Saving session
save.image()

#
# Exporting / Importing CSV
# By default writing into the working directory
# Note: if header is not included, use the parameter header = FALSE
#
write.csv(pt_data, file="pt_data.csv",row.names = FALSE)
data_pt<-read.csv(file = "pt_data.csv",stringsAsFactors = TRUE)
data_pt

#
# Importing XLSX
#
library(readxl)
responses <- read_excel("f1.xlsx")
View(responses)

# Reading tsv
responses <- read.delim(file="f1.tsv")
View(responses)

#
# Used Cars Data Analysis (source usedcars.csv)
#
# Removing everything before analysis of Used Cars (usedcard.csv)
rm(list=ls())

usedcars <- read.csv(file="usedcars.csv")

# Data dictionary
str(usedcars)


