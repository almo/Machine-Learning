#
# Copyright 2025 Andrés Leonardo Martínez Ortiz
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


#
# Universidad Nacional de Educación a Distancia (UNED)
# Grado de Administración y Dirección de Empresas
# Machine Learning Aplicado a Ciencias Sociales Curso 2024/25
# Practicum

#
# Setup - Seed and Libraries
#
source('setting-up.r')

# 
# Loading
#

# Loading main dataset
NFCS.2021 <- read.csv('Datasets/nfcs-2021.csv', stringsAsFactors = TRUE)

# Loading definition and refactoring of data frames
source('demographics.r')
source('capabilities.r')
source('stress.r')

# Loading data preparation
source('grooming-data.r')

# Missing values
source('missing-values.r')

#
# Clustering Analysis
#

#
# Principal component analysis for raw data
# Multiple Correspondence Analysis
#
# Demographics
nrows <- nrow(NFCS.2021.Demographics)
NFCS.2021.Demographics.PA.Sample <- sample(nrows,nrows*0.1)
NFCS.2021.Demographics.PA <- NFCS.2021.Demographics[NFCS.2021.Demographics.PA.Sample,-1]

NFCS.2021.Demographics.PA.Result <- MCA(NFCS.2021.Demographics.PA)
fviz_mca_biplot(NFCS.2021.Demographics.PA.Result)
fviz_mca_ind(NFCS.2021.Demographics.PA.Result)
fviz_eig(NFCS.2021.Demographics.PA.Result)
fviz_mca_var(NFCS.2021.Demographics.PA.Result)

# Financial Capabilities
nrows <- nrow(NFCS.2021.Capabilities)
NFCS.2021.Capabilities.PA.Sample <- sample(nrows,nrows*0.1)
NFCS.2021.Capabilities.PA <- NFCS.2021.Capabilities[NFCS.2021.Capabilities.PA.Sample,-1]

NFCS.2021.Capabilities.PA.Result <- MCA(NFCS.2021.Capabilities.PA)
fviz_mca_biplot(NFCS.2021.Capabilities.PA.Result)
fviz_mca_ind(NFCS.2021.Capabilities.PA.Result)
fviz_eig(NFCS.2021.Capabilities.PA.Result)
fviz_mca_var(NFCS.2021.Capabilities.PA.Result)

# Financial Stress
nrows <- nrow(NFCS.2021.Stress)
NFCS.2021.Stress.PA.Sample <- sample(nrows,nrows*0.1)
NFCS.2021.Stress.PA <- NFCS.2021.Stress[NFCS.2021.Stress.PA.Sample,-1]

NFCS.2021.Stress.PA.Result <- MCA(NFCS.2021.Stress.PA)
fviz_mca_biplot(NFCS.2021.Capabilities.PA.Result)
fviz_mca_ind(NFCS.2021.Capabilities.PA.Result)
fviz_eig(NFCS.2021.Capabilities.PA.Result)
fviz_mca_var(NFCS.2021.Capabilities.PA.Result)

#
# Finding the right cluster Elbow Method
#
gower.dist <- daisy(NFCS.2021.Stress.PA, metric = c("gower"))

wss <- sapply(2:10, function(k) {
  pam_fit <- pam(gower.dist, k = k, diss = TRUE)
  mean(pam_fit$clusinfo[, "av_diss"])
})

plot(2:10, wss, type = "b", pch = 19, frame = FALSE, 
     xlab = "Number of clusters (k)",
     ylab = "Average within-cluster dissimilarity")

#
# Finding the right cluster Silhouette Method
#
sil_widths <- sapply(2:10, function(k) {
  pam_fit <- pam(gower.dist, k = k, diss = TRUE)  # Use PAM clustering
  mean(silhouette(pam_fit)[, "sil_width"])
})

# Plot the silhouette widths
plot(2:10, sil_widths, type = "b", pch = 19, frame = FALSE, 
     xlab = "Number of clusters (k)",
     ylab = "Average silhouette width")

optimal_k <- which.max(sil_widths) + 1  # +1 to adjust for starting at k=2


# Financial Stress
# kmodes Clustering
StressTaxonomy <- kmodes(NFCS.2021.Stress[, -1], modes = 3)

# Size of the clusters
StressTaxonomy$size

# Centroids
StressTaxonomy$modes

# Distance
StressTaxonomy$withindiff

fviz_cluster(list(data = NFCS.2021.Stress[, -1], cluster = StressTaxonomy$cluster))

#
# Extending the Financial Stress dataset including the Stress Group
#
StressGroup <- StressTaxonomy$cluster

StressGroup <- factor(
  StressGroup,
  levels = c(3, 2, 1),
  labels = c("No Stress", "Stress Risk", "Stress")
)

# New data frame for supervised learning
NFCS.2021.MLACS <- merge(NFCS.2021.Demographics,
                         NFCS.2021.Capabilities,
                         by = 'NFCSID',
                         all.x = TRUE)

NFCS.2021.MLACS <- merge(NFCS.2021.MLACS,
                         NFCS.2021.Stress,
                         by = 'NFCSID',
                         all.x = TRUE)

NFCS.2021.MLACS$StressGroup <- StressGroup

# Demographics
NFCS.2021.Demographics$StressGroup <- StressGroup

# Financial Capabilities
NFCS.2021.Capabilities$StressGroup <- StressGroup

# Financial Stress
NFCS.2021.Stress$StressGroup <- StressGroup

#
# Analysis of the Stress Groups and economical analysis
#
StressCounts <- table(NFCS.2021.Stress$StressGroup)
StressCounts <- data.frame(StressGroup = names(StressCounts),
                           n = as.numeric(StressCounts))

ggplot(NFCS.2021.Stress, aes(x = StressGroup)) +
  geom_bar(fill = "steelblue") +  # Use a light blue color for the bars
  geom_text(data = StressCounts, aes(label = n, y = n), vjust = -0.5) +
  labs(title = "Distribución del Nivel de Estrés", x = "Nivel de Estrés", y = "Número de Individuos") +
  theme_minimal()

# Demographic Analysis
StressGroupsState <- table(NFCS.2021.Demographics$StressGroup,
                           NFCS.2021.Demographics$STATEQ)

barplot(
  StressGroupsState,
  beside = TRUE,
  xlab = "Nivel de Estrés",
  ylab = "Numero de Individups",
  main = "Nivel de Estrés por Estado"
)

StressGroupsIncome <- table(NFCS.2021.Demographics$StressGroup,
                            NFCS.2021.Demographics$A8_2021)

barplot(
  StressGroupsIncome,
  beside = TRUE,
  xlab = "Nivel de Estrés",
  ylab = "Numero de Individups",
  main = "Nivel de Estrés por Ingresos"
)

IncomeStressGroups <- table(NFCS.2021.Demographics$A8_2021,
                            NFCS.2021.Demographics$StressGroup)

barplot(
  IncomeStressGroups,
  beside = TRUE,
  xlab = "Nivel de Estrés",
  ylab = "Numero de Individups",
  main = "Ingresos por Nivel de Estrés"
)

#
# Classification
#
# Modelling StressGroup using Demographics information
# A50A     Gender
# A3Ar_w   Age group
# A6       Marital Status
# A11      Number of children
# A9       Work Status
# A8_2021  Income
# A5_2015  Education

# Training / Testing dataset
nrows <- nrow(NFCS.2021.MLACS)
NFCS.2021.MLACS.Sample <- sample(nrows, nrows * 0.7)
NFCS.2021.MLACS.Train <- NFCS.2021.MLACS[NFCS.2021.MLACS.Sample, ]
NFCS.2021.MLACS.Test <- NFCS.2021.MLACS[-NFCS.2021.MLACS.Sample, ]

prop.table(table(NFCS.2021.MLACS.Train$StressGroup))
prop.table(table(NFCS.2021.MLACS.Test$StressGroup))

#
# Decision Tree
#
NFCS.2021.MLACS.D.DT.Model <- rpart(
  StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015,
  data = NFCS.2021.MLACS.Train,
  method = "class"
)

rpart.plot(NFCS.2021.MLACS.D.DT.Model, extra = 104, nn = TRUE)
NFCS.2021.MLACS.D.DT.Model.Prune <- prune(NFCS.2021.MLACS.D.DT.Model, cp = 0.01)  # Example cp value
rpart.plot(NFCS.2021.MLACS.D.DT.Model.Prune,
           extra = 104,
           nn = TRUE)

predictions.dt.d <- predict(NFCS.2021.MLACS.D.DT.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.dt.d, NFCS.2021.MLACS.Test$StressGroup)

# Random Forest
NFCS.2021.MLACS.D.RF.Model <- randomForest(StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015, data = NFCS.2021.MLACS.Train)
print(NFCS.2021.MLACS.D.RF.Model)
plot(NFCS.2021.MLACS.D.RF.Model)
importance(NFCS.2021.MLACS.D.RF.Model)
varImpPlot(NFCS.2021.MLACS.D.RF.Model)

predictions.rf.d <- predict(NFCS.2021.MLACS.D.RF.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.rf.d, NFCS.2021.MLACS.Test$StressGroup)

# Support Vector Machine
NFCS.2021.MLACS.D.SVM.Model <- svm(
  StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015,
  data = NFCS.2021.MLACS.Train,
  kernel = "radial",
  cost = 10,
  gamma = 0.5
)
summary(NFCS.2021.MLACS.D.SVM.Model)

predictions.svm.d <- predict(NFCS.2021.MLACS.D.SVM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.svm.d, NFCS.2021.MLACS.Test$StressGroup)

# Naive Bayes
NFCS.2021.MLACS.D.NB.Model <- naiveBayes(StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015, data = NFCS.2021.MLACS.Train)
summary(NFCS.2021.MLACS.D.NB.Model)

predictions.nb.d <- predict(NFCS.2021.MLACS.D.NB.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.nb.d, NFCS.2021.MLACS.Test$StressGroup)

# Gradient Boosting Machine
NFCS.2021.MLACS.D.GBM.Model <- gbm(
  StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015,
  data = NFCS.2021.MLACS,
  distribution = "gaussian",
  n.trees = 100,
  interaction.depth = 3
)

summary(NFCS.2021.MLACS.D.GBM.Model)

predictions.gbm.d <- predict(NFCS.2021.MLACS.D.GBM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.gbm.d, NFCS.2021.MLACS.Test$StressGroup)

#
# Classification
#
# Modelling StressGroup using Financial Capabilities information
# B1       Checking Account
# B2       Saving Account
# B31      Mobile Payment
# B42      Mobile Operation (transfers)
# B43      Financial planning (Web/Mobile)
# C1_2012  Retirement plans
# B14      Investment
# EA_1     Owned house
# F1       Number of credit cards
# M4       Financial knowledge
#
# Decision Tree
#
NFCS.2021.MLACS.C.DT.Model <- rpart(
  StressGroup ~ B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
  data = NFCS.2021.MLACS.Train,
  method = "class"
)
summary(NFCS.2021.MLACS.C.DT.Model)
rpart.plot(NFCS.2021.MLACS.C.DT.Model, extra = 104, nn = TRUE)

predictions.dt.c <- predict(NFCS.2021.MLACS.C.DT.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.dt.c, NFCS.2021.MLACS.Test$StressGroup)

# Random Forest
NFCS.2021.MLACS.C.RF.Model <- randomForest(StressGroup ~ B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
                                         data = NFCS.2021.MLACS.Train)
summary(NFCS.2021.MLACS.C.RF.Model)
plot(NFCS.2021.MLACS.C.RF.Model)
importance(NFCS.2021.MLACS.C.RF.Model)
varImpPlot(NFCS.2021.MLACS.C.RF.Model)

predictions.rf.c <- predict(NFCS.2021.MLACS.C.RF.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.rf.c, NFCS.2021.MLACS.Test$StressGroup)

# Support Vector Machine
NFCS.2021.MLACS.C.SVM.Model <- svm(
  StressGroup ~ B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
  data = NFCS.2021.MLACS.Train,
  kernel = "radial",
  cost = 10,
  gamma = 0.5
)
summary(NFCS.2021.MLACS.C.SVM.Model)

predictions.svm.c <- predict(NFCS.2021.MLACS.C.SVM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.svm.c, NFCS.2021.MLACS.Test$StressGroup)

# Naive Bayes
NFCS.2021.MLACS.C.NB.Model <- naiveBayes(StressGroup ~ B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
                                       data = NFCS.2021.MLACS.Train)
summary(NFCS.2021.MLACS.C.NB.Model)

predictions.nb.c <- predict(NFCS.2021.MLACS.C.NB.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.nb.c, NFCS.2021.MLACS.Test$StressGroup)

# Gradient Boosting Machine
NFCS.2021.MLACS.C.GBM.Model <- gbm(
  StressGroup ~ B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
  data = NFCS.2021.MLACS,
  distribution = "gaussian",
  n.trees = 100,
  interaction.depth = 3
)

summary(NFCS.2021.MLACS.C.GBM.Model)

predictions.gbm.c <- predict(NFCS.2021.MLACS.C.GBM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.gbm.c, NFCS.2021.MLACS.Test$StressGroup)


#
# Classification
#
# Modelling StressGroup using Demographic and Financial Capabilities information 
# A50A     Gender
# A3Ar_w   Age group
# A6       Marital Status
# A11      Number of children
# A9       Work Status
# A8_2021  Income
# A5_2015  Education
# B1       Checking Account
# B2       Saving Account
# B31      Mobile Payment
# B42      Mobile Operation (transfers)
# B43      Financial planning (Web/Mobile)
# C1_2012  Retirement plans
# B14      Investment
# EA_1     Owned house
# F1       Number of credit cards
# M4       Financial knowledge
#
# Decision Tree
#
NFCS.2021.MLACS.DT.Model <- rpart(
  StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
  data = NFCS.2021.MLACS.Train,
  method = "class"
)
summary(NFCS.2021.MLACS.DT.Model)
rpart.plot(NFCS.2021.MLACS.DT.Model, extra = 104, nn = TRUE)

predictions.dt <- predict(NFCS.2021.MLACS.DT.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.dt, NFCS.2021.MLACS.Test$StressGroup)

# Random Forest
NFCS.2021.MLACS.RF.Model <- randomForest(StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
                                           data = NFCS.2021.MLACS.Train)
summary(NFCS.2021.MLACS.RF.Model)
plot(NFCS.2021.MLACS.RF.Model)
importance(NFCS.2021.MLACS.RF.Model)
varImpPlot(NFCS.2021.MLACS.RF.Model)

predictions.rf <- predict(NFCS.2021.MLACS.RF.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.rf, NFCS.2021.MLACS.Test$StressGroup)

# Support Vector Machine
NFCS.2021.MLACS.SVM.Model <- svm(
  StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
  data = NFCS.2021.MLACS.Train,
  kernel = "radial",
  cost = 10,
  gamma = 0.5
)
summary(NFCS.2021.MLACS.SVM.Model)

predictions.svm <- predict(NFCS.2021.MLACS.SVM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.svm, NFCS.2021.MLACS.Test$StressGroup)

# Naive Bayes
NFCS.2021.MLACS.NB.Model <- naiveBayes(StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
                                         data = NFCS.2021.MLACS.Train)
summary(NFCS.2021.MLACS.NB.Model)

predictions.nb <- predict(NFCS.2021.MLACS.NB.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.nb, NFCS.2021.MLACS.Test$StressGroup)

# Gradient Boosting Machine
NFCS.2021.MLACS.GBM.Model <- gbm(
  StressGroup ~ A50A + A3Ar_w + A6 + A11 + A9 + A8_2021 + A5_2015 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + M4,
  data = NFCS.2021.MLACS,
  distribution = "gaussian",
  n.trees = 100,
  interaction.depth = 3
)

summary(NFCS.2021.MLACS.GBM.Model)

predictions.gbm <- predict(NFCS.2021.MLACS.GBM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.gbm, NFCS.2021.MLACS.Test$StressGroup)
