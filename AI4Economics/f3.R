#
# Lazy Learning - Classification Using Nearest Neighbors
#
# Resources
# https://github.com/PacktPublishing/Machine-Learning-with-R-Fourth-Edition
#
# In general nearest neighbor classifier are well suited for classification
# tasks where relationships among the features and the target classes are
# numerous, complicated, or otherwise extremely difficult to understand, yet
# the items of similar class types tend to be fairly homogeneous i.e You Know
# When You See It (YKWYSI).
#
#
# The K-NN algorithm gets its name from the fact that it uses information about
# an example's K nearest neighbors to classify unlabeled examples.
# The letter K is a variable implying that any number of nearest neighbors
# could be used. After choosing k, the algorithm requires a training dataset
# made up of examples that have been classified into several categories, as
# labeled by a nominal variable. Then, for each unlabeled record in the test
# dataset, K-NN identifies the k records in the training data that are the
# "nearest" in similarity. The unlabeled test Instances is assigned the class
# representing the majority of the k nearest neighbors.
#
# STRENGHTS:
#   - Simple and effective
#   - Make no assumptions about the underlying data distribution
#   - Fast training phase.
#
# WEAKNESSES
#   - Does not produce a model, limiting the ability to understand how the
#     features are related to the class
#   - Requires selection of an appropriate k
#   - Slow classification phase
#   - Nominal features and missing data required additional processing
#
# Parameters and Data Preparation
# k - One common approach is to begin with K equal to the square root of the
#     number of training examples. Alternatively, several k can be used,
#     choosing the one that deliver the best classification performance.
# Re-scaling - the traditional method of re-scaling features for k-NN is the
#     min-max normalization. Alternatively, z-score standardization is used.
# Nominal coding: allows to calculate the distance between nominal features,
#     assigning numeric value to the different categories.
#     Dummy coding: n-1 levels for the feature with n categories, from 1 to n-1.
#     One-hot encoding: creates binary features for all n levels and thus only
#     one will be 1 and the rest 0. This method can trouble with linear models.
#

# Loading dataset Breast Cancer Diagnosis and Prognosis
wbcd <- read.csv("wisc_bc_data.csv")
str(wbcd)

# Removing id
wbcd <- wbcd[-1]
str(wbcd)

# Re-coding diagnosis as factor
table(wbcd$diagnosis)
wbcd$diagnosis <- factor(wbcd$diagnosis, levels = c("B","M"),
                         labels = c("Benign","Malignant"))
round(prop.table(table(wbcd$diagnosis))*100, digits = 1)

# Radius, area and smoothness present quite different scales, so normalization
# is required
summary(wbcd[c("radius_mean","area_mean","smoothness_mean")])

# Using min-max normalization. Note: future values might fall outside of the
# min-max range of the testing dataset.
normalize <- function(x){
  return ((x - min(x))/(max(x)-min(x)))
}
normalize(c(1,2,3,4,5))
normalize(c(1,2,3,4,5,6,7))
normalize(c(10,20,30,40,50))

wbcd_n <- as.data.frame(lapply(wbcd[2:31],normalize))
str(wbcd_n)
summary(wbcd_n[c("radius_mean","area_mean","smoothness_mean")])

# Simulating a real scenario splitting the dataset in training and testing
# Note: Assuming dataset is randonly ordered. Otherwise, it has to be randomize.
wbcd_train <- wbcd_n[1:469,]
wbcd_test <- wbcd_n[470:569,]
str(wbcd_train)
str(wbcd_test)
# Labels
wbcd_train_labels <- wbcd[1:469,1]
wbcd_test_labels <- wbcd[470:569,1]

install.packages("class")
library(class)

# Building the classifier and estimating the test dataset
wbcd_test_pred <- knn(train = wbcd_train, test = wbcd_test, cl = wbcd_train_labels, k=21)

# Evaluating model performance
library(gmodels)
CrossTable(x = wbcd_test_labels, y = wbcd_test_pred, prop.chisq = FALSE)

# Improving model performance
# Option A: using z-score standardization
wbcd_z <- as.data.frame(scale(wbcd[-1]))
summary(wbcd_z)
summary(wbcd_z$area_mean)

# Training / Test dataset. labels stay the same.
wbcd_train <- wbcd_z[1:469,]
wbcd_test <- wbcd_z[470:569,]

wbcd_test_pred <- knn(train = wbcd_train, test = wbcd_test, cl = wbcd_train_labels, k=21)
CrossTable(x = wbcd_test_labels, y = wbcd_test_pred, prop.chisq = FALSE)

# Testing several k
wbcd_test_pred <- knn(train = wbcd_train, test = wbcd_test, cl = wbcd_train_labels, k=5)
CrossTable(x = wbcd_test_labels, y = wbcd_test_pred, prop.chisq = FALSE)

wbcd_test_pred <- knn(train = wbcd_train, test = wbcd_test, cl = wbcd_train_labels, k=10)
CrossTable(x = wbcd_test_labels, y = wbcd_test_pred, prop.chisq = FALSE)

wbcd_test_pred <- knn(train = wbcd_train, test = wbcd_test, cl = wbcd_train_labels, k=15)
CrossTable(x = wbcd_test_labels, y = wbcd_test_pred, prop.chisq = FALSE)
