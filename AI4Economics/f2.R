#
# Reading information about used cars
#
rm (list=ls())
usedcars <- read.csv(file="usedcars.csv")
str(usedcars)

#
# Discrete stadistics
#
summary(usedcars$year)
summary(usedcars[c("price","mileage")])

#
# Measuring average
#
mean(usedcars$price)
median(usedcars$price)

#
# Measuring spread
#
range(usedcars$price)
diff(range(usedcars$price))

# Quantiles
IQR(usedcars$price)
# Quartiles
quantile(usedcars$price)
# deciles
quantile(usedcars$price, probs=seq(0,1,0.1))

# arbitrary quantiles
quantile(usedcars$price, probs=c(0.17,0.43,0.91))
?quantile

#
# Visualizing
#

#boxplot
boxplot(usedcars$price, main="Boxplot of Used Car Prices",ylab="Price ($)",
        notch=TRUE)
boxplot(usedcars$mileage, main="Boxplot of Used Car Mileage",
        ylab="Odometer (mi.)",varwidth = TRUE)
?boxplot

# The minimum and the maximum values can be illustrated using whiskers that
# extend below and above the box; however, a widely used convention only allows
# the whiskers to extend to a minimum or maximum of 1.5 times the IQR below Q1
# and Q3. Any values that fall beyond this threshold are considered outliers and
# as circles or dots.
#
#

#
# Histograms
#
hist(usedcars$price, main="Histogram of Used Car Prices", xlab="Price ($)")
hist(usedcars$mileage, main="Histogram of Used Car Mileage",
     xlab="Odometer (mi.)")

hist(usedcars$mileage, main="Histogram of Used Car Mileage",
     xlab="Odometer (mi.)",breaks = 20)

#
# Variance and standard deviation
# Note: R uses sample variance (which divides by n-1) instead of population
#       variance (which divides by n)
var(usedcars$price)
sd(usedcars$price)

#
# Categorical features
#
table(usedcars$year)
table(usedcars$model)
table(usedcars$color)

# Distributions
color_table <- table(usedcars$color)
color_pct <- prop.table(color_table) * 100
round(color_pct, digits = 1)

#
# Scatter plots (bivariate relationships among quantitative variables)
#
plot(x=usedcars$mileage, y=usedcars$price,
     main="Scatterplot of Price vs Mileage",
     xlab="Used Car Odometer (mi.)",
     ylab="Used Car Price ($)")

#
# Two-way cross-tabulation or contingency table
# Bivariate relationships among qualitative (nominal) variable
#
#install.packages("gmodels")
library(gmodels)

# Simplifying the colors, reducing the number i.e. conservative (TRUE/FALSE)
usedcars$conservative <- usedcars$color %in% c("Black","Gray","Silver","White")
table(usedcars$conservative)

CrossTable(x=usedcars$model, y=usedcars$conservative)
# Chi-squared test for independence between two variables
# ~ 1.0 independence / ~ 0.0 dependence
# 0.154 = 0.009+0.004+0.086+0.044+0.007+0.004
pchisq(0.009+0.004+0.086+0.044+0.007+0.004,df=2,lower.tail = FALSE)
CrossTable(x=usedcars$model, y=usedcars$conservative,chisq = TRUE)
