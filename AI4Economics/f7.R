#
# Black-Box Methods - Neural Networks and Support Vector Machines
#
# Resources
# https://github.com/PacktPublishing/Machine-Learning-with-R-Fourth-Edition
# Chapter Seven
#

letters <- read.csv("letterdata.csv", stringsAsFactors = TRUE)
str(letters)

letters_train <- letters[1:16000,]
letters_test <- letters[16001:20000,]

# SVM Packages
# http://www.csie.ntu.edu.tw/~cjlin/libsvm
install.packages("e1071")
library(e1071)

# https://www.cs.cornell.edu/people/tj/svm_light/
install.packages("klaR")
library(klaR)

# http://www.jstatsoft.org/v11/i09
install.packages("kernlab")
library(kernlab)

letter_classifier <- kernlab::ksvm(letter ~., data=letters_train, kernel="vanilladot")
letter_classifier

letter_predictions <- predict (letter_classifier, letters_test)
head(letter_predictions)
table(letter_predictions, letters_test$letter)

agreement <- letter_predictions == letters_test$letter
table(agreement)
prop.table((table(agreement)))


set.seed(1234567)
letter_classifier_rbf <- ksvm(letter ~ ., data=letters_train, kernel="rbfdot")
letter_predictions_rbf <- predict(letter_classifier_rbf, letters_test)
agreement_rbf <- letter_predictions_rbf == letters_test$letter
table(agreement_rbf)
prop.table(table(agreement_rbf))


cost_values <- c(1, seq(from=5, to =40, by=5))
accuracy_values <- sapply(cost_values, function(x){
  set.seed(98765)
  m <- ksvm(letter ~., data=letters_train, kernel="rbfdot",C=x)
  pred <- predict(m,letters_test)
  agree <- ifelse(pred==letters_test$letter,1,0)
  accuracy <- sum(agree)/nrow(letters_test)
  return(accuracy)
})
plot(cost_values,accuracy_values, type="b")
