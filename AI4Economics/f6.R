#
# Forecasting Numeric Data - Regression Methods
#
# Resources
# https://github.com/PacktPublishing/Machine-Learning-with-R-Fourth-Edition
# Chapter Six
#

launch <- read.csv("challenger.csv")
str(launch)

# Single Regression
# Obtaining regression parameters for Distress-CT = A + B Temprature
b <- cov(launch$temperature,launch$distress_ct) / var (launch$temperature)
b

a <- mean (launch$distress_ct) - b * mean(launch$temperature)
a

r <-  cov(launch$temperature, launch$distress_ct) / (sd(launch$temperature) * sd(launch$distress_ct))
r

# Multiple Regression
# Function obtaining parameters using matrix algebra
# B(parameter) = (X^T * X)^-1 * X^T * Y
reg <- function (y,x){
  x <- as.matrix(x)
  x <- cbind(Intercept=1, x)
  # solve inverts a matrix
  # %*% multiplies matrix
  b <- solve(t(x) %*% x ) %*% t(x) %*% y
  colnames (b) <- "estimate"
  print(b)
}

# Simple Regresion using matricial (previous example)
reg (y=launch$distress_ct, x=launch[2])

# Multiple Regression
reg(y=launch$distress_ct, x=launch[2:4])


#
# Generalized Linear Models (counting values, categorical or binary)
# loosens two assumptions of traditional regression modeling: (1) target variable
# can be non-normally distributed, non-continuous and (2) the variance of target
# variable might be related to its mean.
# As results
# 1) Dependent variable might be chosen from the exponential family, including
#    Poisson, Binomial and Gamma.
# 2) The link function transform the relationship between the predictors and the
#    target, such as it can be modeled using linear equation.
#


insurance <- read.csv("autoinsurance.csv", stringsAsFactors = TRUE)
str(insurance)
summary(insurance$expenses)
hist(insurance$expenses)


table(insurance$geo_area)
table(insurance$vehicle_type)

cor(insurance[c("age","est_value", "miles_driven", "expenses")])
pairs(insurance[c("age", "est_value","miles_driven", "expenses")], pch=".")

install.packages("psych")
library("psych")
pairs.panels(insurance[c("age", "est_value","miles_driven", "expenses")], pch=".")


ins_model <- lm(expenses ~ age + geo_area + vehicle_type + est_value +
                  miles_driven + college_grad_ind + speeding_ticket_ind +
                  hard_braking_ind + late_driving_ind + clean_driving_ind,
                data=insurance)
ins_model <- lm(expenses ~ ., data=insurance)
options(scipen=999)
ins_model
summary (ins_model)

# Improving the model
insurance$age2 <- insurance$age^2
str(insurance)

ins_model2 <- lm(expenses ~ . + hard_braking_ind:late_driving_ind, data=insurance)
summary(ins_model2)

# Prediction
insurance$pred <- predict(ins_model2, insurance)
cor (insurance$pred, insurance$expenses)
plot(insurance$pred, insurance$expenses)
abline(a=0,b=1,col="red", lwd=3,lty=2)


# Churn Model
churn_data <- read.csv("insurance_churn.csv")
prop.table(table(churn_data$churn))

churn_model <- glm(churn ~ . - member_id, data=churn_data,
                   family = binomial(link="logit"))
summary(churn_model)

churn_test <- read.csv("insurance_churn_test.csv")
churn_test$churn_prob <- predict(churn_model, churn_test, type="response")
summary(churn_test$churn_prob)

churn_order <- order(churn_test$churn_prob, decreasing = TRUE)
head(churn_test[churn_order,c("member_id","churn_prob")],n=10)

# Model Trees
# Model Trees extended Regression Trees by replacing the leaf nodes with
# regression models
wine <- read.csv("whitewines.csv")
str(wine)
hist(wine$quality)
summary(wine)

wine_train <- wine[1:3750,]
wine_test <- wine[3751:4898,]

install.packages("rpart")
library(rpart)

m.rpart <- rpart(quality ~ ., data=wine_train)
m.rpart
summary(m.rpart)


install.packages("rpart.plot")
library(rpart.plot)

rpart.plot(m.rpart, digits=3)
rpart.plot(m.rpart, digits=4, fallen.leaves = TRUE, type=3, extra=101)

p.rpart <- predict(m.rpart, wine_test)
summary(p.rpart)
summary(wine_test$quality)
cor(p.rpart,wine_test$quality)

MAE <- function(actual, predicted){
  mean(abs(actual-predicted))
}

MAE(wine_test$quality, p.rpart)

install.packages("Cubist")
library(Cubist)
m.cubist <- cubist(x=wine_train[-12],y=wine_train$quality)
m.cubist
summary(m.cubist)
p.cubist <- predict(m.cubist, wine_test)
summary(p.cubist)
