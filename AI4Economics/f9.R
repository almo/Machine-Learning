#
# Finding Groups of Data - Clustering with k-means
#
# Resources
# https://github.com/PacktPublishing/Machine-Learning-with-R-Fourth-Edition
# Chapter Nine
#

# Some advanced clustering method such as mixture modelling and Density-Based
# Spatial Clustering of Application with Noise (DBSCAN) are not covered in the
# chapter. However, these are the packages in R mclust and dbscan. These can be
# installed as usual with install.packages('mclust') and
# install.packages('dbscan')
setwd("/home/almo/Development/rlang/Machine-Learning/AI4Economics")
if (!require(dbscan)) install.packages('dbscan', dependencies = TRUE)
library(dbscan)

if (!require(mclust)) install.packages('mclust', dependencies = TRUE)
library(mclust)

# K-Means algorithm uses a distance function, and thus all features should be
# numerical and normalized.

teens <- read.csv('snsdata.csv', stringsAsFactors = TRUE)
str(teens)

# Gender feature presents missing data (NA) ~ 2700 entries
table(teens$gender)
table(teens$gender, useNA="ifany")
summary(teens$gender)

# Age feature presents missing values and outliers (3 years / 106)
summary(teens$age)
teens$age <- ifelse(teens$age >= 13 & teens$age < 20, teens$age, NA)
summary(teens$age)

# For categorical features, the to address the missing values is using dummy
# variables. In this case, a new no_gender feature is added
teens$female <- ifelse(teens$gender=="F" & !is.na(teens$gender),1,0)
teens$no_gender <- ifelse(is.na(teens$gender),1,0)
table(teens$gender, useNA="ifany")
table(teens$female, useNA="ifany")
table(teens$no_gender, useNA="ifany")

# For numerical features, the approach is different of dummy variables;
# imputation is used instead. In this case, the value is the mean. In order
# to calculate the mean, NA has to be removed.
mean(teens$age, na.rm=TRUE)
aggregate(data = teens, age ~ gradyear, mean, na.rm = TRUE)

# Instead of obtaining the frame with the mean, we need a vector assigning the
# average to each entre
ave_age <- ave (teens$age, teens$gradyear, FUN = function(x) mean (x, na.rm = TRUE))
summary (ave_age)

# and then assigning it to the NA entries
teens$age <- ifelse(is.na(teens$age), ave_age, teens$age)
summary (teens$age)

#  Now we exclude demographics and focus only on interest
interests <- teens[5:40]
# and now, normilization
interests_z <- as.data.frame(lapply(interests,scale))
summary (interests$basketball)
summary (interests_z$basketball)

set.seed(923841)
teen_cluster <-  kmeans(interests_z, 5)
teen_cluster$size
teen_cluster$centers
#if (!require(remotes)) install.packages("remotes", dependencies=TRUE)
#library(remotes)

#if (!require(Matrix)) install.packages("Matrix", dependencies = TRUE)
#remotes::install_version("Matrix", version = "1.6.0")
library(Matrix)

#if (!require(pbkrtest)) install.packages("pbkrtest", dependencies = TRUE)
#library(pbkrtest)

if (!require(factoextra)) install.packages("factoextra", dependencies = TRUE)
library(factoextra)

#if (!require(rstatix)) install.packages("rstatix", dependencies=TRUE)
#library(rstatix)

#if (!require(car)) install.packages("car", dependencies=TRUE)
#library(car)


fviz_cluster(teen_cluster,interests_z,geom="point")
