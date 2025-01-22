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
source("setting-up.r")

#
# Loading
#

# Loading main dataset
NFCS.2021 <- read.csv("Datasets/nfcs-2021.csv", stringsAsFactors = TRUE)

# Loading definition and refactoring of data frames
source("demographics.r")
source("capabilities.r")
source("stress.r")

# Loading data preparation
source("grooming-data.r")

# Missing values
source("missing-values.r")

#
# Clustering Analysis
#

#
# Principal component analysis for raw data
# Multiple Correspondence Analysis
#
# Financial Stress
nrows <- nrow(NFCS.2021.Stress)
NFCS.2021.Stress.MCA.Sample <- sample(nrows, nrows * 0.02)
NFCS.2021.Stress.MCA <- NFCS.2021.Stress[NFCS.2021.Stress.MCA.Sample, -c(1)]

NFCS.2021.Stress.MCA.Result <- MCA(NFCS.2021.Stress.MCA, level.ventil = 0.1)
fviz_mca_ind(NFCS.2021.Stress.MCA.Result)
fviz_mca_var(NFCS.2021.Stress.MCA.Result,
  col.var = "contrib",
  gradient.cols = c("#00AFBB", "#E7B800", "#FC4E07"),
  repel = TRUE,
  title = "MCA - Categories Plot"
)
fviz_mca_var(NFCS.2021.Stress.MCA.Result,
  ellipse.type = "confidence",
  ellipse.level = 0.99, # 99% confidence ellipses
  ellipse.alpha = 0.5, # Semi-transparent ellipses
  J10 = "group_variable", # Group by 'group_variable'
  repel = TRUE
)
fviz_eig(NFCS.2021.Stress.MCA.Result)
fviz_contrib(NFCS.2021.Stress.MCA.Result, choice = "var", axes = 1, top = 20, sort.val = "desc")
fviz_contrib(NFCS.2021.Stress.MCA.Result, choice = "var", axes = 2, top = 20, sort.val = "desc")

plotellipses(NFCS.2021.Stress.MCA.Result, keepvar = c("J1", "J3", "J4", "J5"))
plotellipses(NFCS.2021.Stress.MCA.Result, keepvar = c("J10", "J20", "J32", "P50"))
plotellipses(NFCS.2021.Stress.MCA.Result, keepvar = c("F2_1", "F2_2", "F2_3", "F2_4", "F2_5", "F2_6"))
plotellipses(NFCS.2021.Stress.MCA.Result, keepvar = c("G20", "H30_1", "H30_2", "H30_3"))
plotellipses(NFCS.2021.Stress.MCA.Result, keepvar = c("G38"))
plotellipses(NFCS.2021.Stress.MCA.Result, keepvar = c("J20", "J32"))

NFCS.2021.Stress.MCA.feature <- cbind.data.frame(NFCS.2021.Stress.MCA[, c("J20")], NFCS.2021.Stress.MCA.Result$ind$coord)
NFCS.2021.Stress.PCA <- PCA(NFCS.2021.Stress.MCA.feature, quali.sup = 1, scale = FALSE, graph = FALSE)
NFCS.2021.Stress.PCA$eig[1:5, ] <- NFCS.2021.Stress.MCA.Result$eig[1:5, ]
concat.data <- cbind.data.frame(NFCS.2021.Stress.MCA[, c("J1")], NFCS.2021.Stress.MCA.Result$ind$coord)
ellipse.coord <- coord.ellipse(concat.data, bary = TRUE)
plot.PCA(NFCS.2021.Stress.PCA, habillage = 1, ellipse = ellipse.coord, cex = 0.8, label = "none")

NFCS.2021.Stress.MCA.feature <- cbind.data.frame(NFCS.2021.Stress.MCA[, c("J32")], NFCS.2021.Stress.MCA.Result$ind$coord)
NFCS.2021.Stress.PCA <- PCA(NFCS.2021.Stress.MCA.feature, quali.sup = 1, scale = FALSE, graph = FALSE)
NFCS.2021.Stress.PCA$eig[1:5, ] <- NFCS.2021.Stress.MCA.Result$eig[1:5, ]
concat.data <- cbind.data.frame(NFCS.2021.Stress.MCA[, c("P50")], NFCS.2021.Stress.MCA.Result$ind$coord)
ellipse.coord <- coord.ellipse(concat.data, bary = TRUE)
plot.PCA(NFCS.2021.Stress.PCA, habillage = 1, ellipse = ellipse.coord, cex = 0.8, label = "none")

#
# Finding the right cluster Elbow Method
#
nrows <- nrow(NFCS.2021.Stress)
NFCS.2021.Stress.MCA.Sample <- sample(nrows, nrows * 0.1)
NFCS.2021.Stress.MCA <- NFCS.2021.Stress[NFCS.2021.Stress.MCA.Sample, -c(1)]

gower.dist <- daisy(NFCS.2021.Stress.MCA, metric = c("gower"))

wss <- sapply(2:10, function(k) {
  pam_fit <- pam(gower.dist, k = k, diss = TRUE)
  mean(pam_fit$clusinfo[, "av_diss"])
})

plot(2:10, wss,
  type = "b", pch = 19, frame = FALSE,
  xlab = "Número de groups (k)",
  ylab = "Distancia WSS media (adaptada)"
)

#
# Finding the right cluster Silhouette Method
#
sil_widths <- sapply(2:10, function(k) {
  pam_fit <- pam(gower.dist, k = k, diss = TRUE) # Use PAM clustering
  mean(silhouette(pam_fit)[, "sil_width"])
})

# Plot the silhouette widths
plot(2:10, sil_widths,
  type = "b", pch = 19, frame = FALSE,
  xlab = "Número de grupos (k)",
  ylab = "Distancia Silhouette media"
)

optimal_k <- which.max(sil_widths) + 1 # +1 to adjust for starting at k=2

#
# kmodes Clustering
#
StressTaxonomy <- kmodes(NFCS.2021.Stress[, -1], modes = 3)

# Size of the clusters
StressTaxonomy$size

# Centroids
StressTaxonomy$modes

# Distance
StressTaxonomy$withindiff

#
# Extending the Financial Stress dataset including the Stress Group
#
StressGroup <- StressTaxonomy$cluster

StressGroup <- factor(
  StressGroup,
  levels = c(1, 2, 3),
  labels = c("No Stress", "Stress Risk", "Stress")
)

# Financial Stress
NFCS.2021.Stress$StressGroup <- StressGroup

# New data frame for supervised learning
NFCS.2021.MLACS <- merge(NFCS.2021.Demographics,
  NFCS.2021.Capabilities,
  by = "NFCSID",
  all.x = TRUE
)

NFCS.2021.MLACS$StressGroup <- StressGroup

# Demographics
NFCS.2021.Demographics$StressGroup <- StressGroup

# Financial Capabilities
NFCS.2021.Capabilities$StressGroup <- StressGroup


#
# Plotting the groups
#
nrows <- nrow(NFCS.2021.MLACS)
NFCS.2021.MLACS.MCA.Sample <- sample(nrows, nrows * 0.1)
NFCS.2021.MLACS.MCA <- NFCS.2021.MLACS[NFCS.2021.MLACS.MCA.Sample, -c(1)]

mca_result <- MCA(NFCS.2021.MLACS.MCA, level.ventil = 0.1)

fviz_mca_ind(mca_result, 
             habillage = "StressGroup", 
             palette = "jco", 
             addEllipses = TRUE, 
             legend.title = "Grupos de Estrés")

#
# Classification
#
# Modelling StressGroup using Demographic and Financial Capabilities information
# STATEQ   State
# A50A     Gender
# A3Ar_w   Age group
# A50B     Gender / Age group
# A5_2015  Education
# A6       Marital Status
# A7       Living arrangement
# A11      Number of children
# A8_2021  Income
# A9       Work Status
# A40      Multiple jobs
# A41      Parents' education
# B1       Checking Account
# B2       Saving Account
# B31      Mobile Payment
# B42      Mobile Operation (transfers)
# B43      Financial planning (Web/Mobile)
# C1_2012  Retirement plans
# B14      Investment
# EA_1     Owned house
# F1       Number of credit cards
# H1       Health Insurance 
# M1_1     Self opinion on financial skills
# M4       Financial knowledge
# M20      Formal financial education (courses)

# Training / Testing dataset
nrows <- nrow(NFCS.2021.MLACS)
NFCS.2021.MLACS.Sample <- sample(nrows, nrows * 0.1)
NFCS.2021.MLACS.Train <- NFCS.2021.MLACS[NFCS.2021.MLACS.Sample, ]
NFCS.2021.MLACS.Test <- NFCS.2021.MLACS[-NFCS.2021.MLACS.Sample, ]

prop.table(table(NFCS.2021.MLACS.Train$StressGroup))
prop.table(table(NFCS.2021.MLACS.Test$StressGroup))

#
# Random Forest
#
NFCS.2021.MLACS.RF.Model <- randomForest(
  StressGroup ~ STATEQ + A50A + A3Ar_w + A50B + A5_2015 + A6 + A7 + A11 + A8_2021 + A9 + A40 +
                A41 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + H1 + M1_1 + M4 + M20,
  data = NFCS.2021.MLACS.Train
)
summary(NFCS.2021.MLACS.RF.Model)
plot(NFCS.2021.MLACS.RF.Model)
importance(NFCS.2021.MLACS.RF.Model)
varImpPlot(NFCS.2021.MLACS.RF.Model)

predictions.rf <- predict(NFCS.2021.MLACS.RF.Model, NFCS.2021.MLACS.Test, type = "class")
table(predictions.rf, NFCS.2021.MLACS.Test$StressGroup)

# Model Evaluation
CrossTable(NFCS.2021.MLACS.Test$StressGroup, predictions.rf)
confusionMatrix(predictions.rf,NFCS.2021.MLACS.Test$StressGroup)

#
# Support Vector Machine
#
NFCS.2021.MLACS.SVM.Model <- svm(
    StressGroup ~ STATEQ + A50A + A3Ar_w + A50B + A5_2015 + A6 + A7 + A11 + A8_2021 + A9 + A40 +
                A41 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + H1 + M1_1 + M4 + M20,
  data = NFCS.2021.MLACS.Train,
  kernel = "radial",
  cost = 10,
  gamma = 0.5
)
summary(NFCS.2021.MLACS.SVM.Model)

predictions.svm <- predict(NFCS.2021.MLACS.SVM.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.svm, NFCS.2021.MLACS.Test$StressGroup)

# Model Evaluation
CrossTable(NFCS.2021.MLACS.Test$StressGroup, predictions.svm)
confusionMatrix(predictions.svm,NFCS.2021.MLACS.Test$StressGroup)

#
# Naive Bayes
#
NFCS.2021.MLACS.NB.Model <- naiveBayes(
    StressGroup ~ STATEQ + A50A + A3Ar_w + A50B + A5_2015 + A6 + A7 + A11 + A8_2021 + A9 + A40 +
                A41 + B1 + B2 + B31 + B42 + B43 + C1_2012 + B14 + EA_1 + F1 + H1 + M1_1 + M4 + M20,
  data = NFCS.2021.MLACS.Train
)
summary(NFCS.2021.MLACS.NB.Model)

predictions.nb <- predict(NFCS.2021.MLACS.NB.Model, newdata = NFCS.2021.MLACS.Test)
table(predictions.nb, NFCS.2021.MLACS.Test$StressGroup)

# Model Evaluation
CrossTable(NFCS.2021.MLACS.Test$StressGroup, predictions.nb)
confusionMatrix(predictions.nb,NFCS.2021.MLACS.Test$StressGroup)


#
# Analysis of the Stress Groups and economical analysis
#

#
# Dataset filtered by Stress Group
#
NFCS.2021.MLACS.NoStress <- NFCS.2021.MLACS %>% filter(StressGroup == "No Stress")
NFCS.2021.MLACS.StressRisk <- NFCS.2021.MLACS %>% filter(StressGroup == "Stress Risk")
NFCS.2021.MLACS.Stress <- NFCS.2021.MLACS %>% filter(StressGroup == "Stress")

# No Stress
nrows <- nrow(NFCS.2021.MLACS.NoStress)
NFCS.2021.MLACS.NoStress.Sample <- sample(nrows, nrows * 0.7)
NFCS.2021.MLACS.NoStress.Train <- NFCS.2021.MLACS.NoStress[NFCS.2021.MLACS.NoStress.Sample, ]
NFCS.2021.MLACS.NoStress.Test <- NFCS.2021.MLACS.NoStress[-NFCS.2021.MLACS.NoStress.Sample, ]
# MCA
nrows <- nrow(NFCS.2021.MLACS.NoStress)
NFCS.2021.MLACS.NoStress.MCA.Sample <- sample(nrows, nrows * 0.1)
NFCS.2021.MLACS.NoStress.MCA <- NFCS.2021.MLACS.NoStress[NFCS.2021.MLACS.NoStress.MCA.Sample, -c(1)]

mca_result <- MCA(NFCS.2021.MLACS.NoStress.MCA)

fviz_mca_ind(mca_result, 
             habillage = "A3Ar_w",
             palette = "jco", 
             addEllipses = TRUE, 
             legend.title = "MCA - No Estrés")

# Stress Risk
nrows <- nrow(NFCS.2021.MLACS.StressRisk)
NFCS.2021.MLACS.StressRisk.Sample <- sample(nrows, nrows * 0.7)
NFCS.2021.MLACS.StressRisk.Train <- NFCS.2021.MLACS.StressRisk[NFCS.2021.MLACS.StressRisk.Sample, ]
NFCS.2021.MLACS.StressRisk.Test <- NFCS.2021.MLACS.StressRisk[-NFCS.2021.MLACS.StressRisk.Sample, ]
# MCA
nrows <- nrow(NFCS.2021.MLACS.StressRisk)
NFCS.2021.MLACS.StressRisk.MCA.Sample <- sample(nrows, nrows * 0.1)
NFCS.2021.MLACS.StressRisk.MCA <- NFCS.2021.MLACS.StressRisk[NFCS.2021.MLACS.StressRisk.MCA.Sample, -c(1)]

mca_result <- MCA(NFCS.2021.MLACS.StressRisk.MCA)

fviz_mca_ind(mca_result, 
             habillage = "A8_2021", 
             palette = "jco", 
             addEllipses = TRUE, 
             legend.title = "MCA - Riesgo Estrés")


# Stress
nrows <- nrow(NFCS.2021.MLACS.Stress)
NFCS.2021.MLACS.StressSample <- sample(nrows, nrows * 0.7)
NFCS.2021.MLACS.Stress.Train <- NFCS.2021.MLACS.Stress[NFCS.2021.MLACS.Stress.Sample, ]
NFCS.2021.MLACS.Stress.Test <- NFCS.2021.MLACS.Stress[-NFCS.2021.MLACS.Stress.Sample, ]
# MCA
nrows <- nrow(NFCS.2021.MLACS.Stress)
NFCS.2021.MLACS.Stress.MCA.Sample <- sample(nrows, nrows * 0.1)
NFCS.2021.MLACS.Stress.MCA <- NFCS.2021.MLACS.Stress[NFCS.2021.MLACS.Stress.MCA.Sample, -c(1)]

mca_result <- MCA(NFCS.2021.MLACS.Stress.MCA)
fviz_mca_ind(mca_result, 
             habillage = "A6", 
             palette = "jco", 
             addEllipses = TRUE, 
             legend.title = "MCA - Estrés")

#
# Visualization of Categorical Features
#
for (col in names(NFCS.2021.MLACS.Stress[,-c(1,27)])) {
  barplot(table(NFCS.2021.MLACS.Stress[[col]]), 
          col = topo.colors(length(unique(NFCS.2021.MLACS.Stress[[col]]))),
          main = paste("Distribución de ", col," para individuos con Stress Financiero"),
          xlab = col, ylab = "Frequency")
}

for (col in names(NFCS.2021.MLACS.NoStress[,-c(1,27)])) {
  barplot(table(NFCS.2021.MLACS.Stress[[col]]), 
          col = rainbow(length(unique(NFCS.2021.MLACS.NoStress[[col]]))),
          main = paste("Distribución de ", col," para individuos sin Estrés Financiero"),
          xlab = col, ylab = "Frequency")
}

for (col in names(NFCS.2021.MLACS.StressRisk[,-c(1,27)])) {
  barplot(table(NFCS.2021.MLACS.StressRisk[[col]]), 
          col = terrain.colors(length(unique(NFCS.2021.MLACS.StressRisk[[col]]))),
          main = paste("Distribución de ", col," para individuos con Riesgo de Estrés Financiero"),
          xlab = col, ylab = "Frequency")
}

#
# Mosaic and Association
#
# A8_2021 - Income
mosaicplot(table(NFCS.2021.MLACS$A8_2021, NFCS.2021.MLACS$StressGroup), 
           shade = TRUE,  # Highlight significant associations
           main = "Ingresos vs Grupo de Estrés",
           xlab = "A8_2021 (Ingresos)", ylab = "Grupo de Estrés")

assoc(table(NFCS.2021.MLACS$A8_2021, NFCS.2021.MLACS$StressGroup), 
      shade = TRUE,  # Add shading for significance
      main = "Income vs Stress Group")

# A11 - Number of kids
mosaicplot(table(NFCS.2021.MLACS$StressGroup,NFCS.2021.MLACS$A11), 
           shade = TRUE,  # Highlight significant associations
           main = "Número de hijos vs Grupo de Estrés",
           las = 2,
           xlab = "A11 (Número de hijos)", ylab = "Grupo de Estrés")

assoc(table(NFCS.2021.MLACS$A11, NFCS.2021.MLACS$StressGroup), 
      shade = TRUE,  # Add shading for significance
      main = "Número de hijos vs Grupo de Estrés")


# A6 - Status Marital
mosaicplot(table(NFCS.2021.MLACS$A6, NFCS.2021.MLACS$StressGroup), 
           shade = TRUE,  # Highlight significant associations
           main = "Estado Civil vs Grupo de Estrés",
           xlab = "A6 (Estado Civil)", ylab = "Grupo de Estrés")

assoc(table(NFCS.2021.MLACS$A6, NFCS.2021.MLACS$StressGroup), 
      shade = TRUE,  # Add shading for significance
      main = "Estado Civil vs Grupo de Estrés")


# A9 - Employment
mosaicplot(table(NFCS.2021.MLACS$A9, NFCS.2021.MLACS$StressGroup), 
           shade = TRUE,  # Highlight significant associations
           main = "Estado laboral vs Grupo de Estrés",
           xlab = "A9 (Estado laboral)", ylab = "Grupo de Estrés")

assoc(table(NFCS.2021.MLACS$A9, NFCS.2021.MLACS$StressGroup), 
      shade = TRUE,  # Add shading for significance
      main = "Estado laboral vs Grupo de Estrés")

# A5_2015 - Education
mosaicplot(table(NFCS.2021.MLACS$A5_2015, NFCS.2021.MLACS$StressGroup), 
           shade = TRUE,  # Highlight significant associations
           main = "Educacion vs Grupo de Estrés",
           las = 2,
           xlab = "A5_2015 (Educación)", ylab = "Grupo de Estrés")

assoc(table(NFCS.2021.MLACS$A5_2015, NFCS.2021.MLACS$StressGroup), 
      shade = TRUE,  # Add shading for significance
      main = "Educación vs Grupo de Estrés")

# A50A - Gender
mosaicplot(table(NFCS.2021.MLACS$A50A, NFCS.2021.MLACS$StressGroup), 
           shade = TRUE,  # Highlight significant associations
           main = "Genero vs Grupo de Estrés",
           las = 2,
           xlab = "A50A (Genero)", ylab = "Grupo de Estrés")

assoc(table(NFCS.2021.MLACS$A50A, NFCS.2021.MLACS$StressGroup), 
      shade = TRUE,  # Add shading for significance
      main = "Genero vs Grupo de Estrés")

# A3Ar_w - Age
mosaicplot(table(NFCS.2021.MLACS$A3Ar_w, NFCS.2021.MLACS$StressGroup), 
           shade = TRUE,  # Highlight significant associations
           main = "Edad vs Grupo de Estrés",
           las = 1,
           xlab = "A3Ar_w (Edad)", ylab = "Grupo de Estrés")

assoc(table(NFCS.2021.MLACS$A3Ar_w, NFCS.2021.MLACS$StressGroup), 
      shade = TRUE,  # Add shading for significance
      main = "A3Ar_w (Genero) vs Grupo de Estrés")

# H1 - Health Insurance
mosaicplot(table(NFCS.2021.MLACS$H1, NFCS.2021.MLACS$StressGroup), 
           shade = TRUE,  # Highlight significant associations
           main = "Seguro médico vs Grupo de Estrés",
           las = 1,
           xlab = "H1 (Seguro Médico)", ylab = "Grupo de Estrés")

assoc(table(NFCS.2021.MLACS$H1, NFCS.2021.MLACS$StressGroup), 
      shade = TRUE,  # Add shading for significance
      main = "H1 (Seguro médico) vs Grupo de Estrés")

# A11 - Kids
mosaicplot(table(NFCS.2021.MLACS$A11, NFCS.2021.MLACS$StressGroup), 
           shade = TRUE,  # Highlight significant associations
           main = "Educacion vs Grupo de Estrés",
           las = 2,
           xlab = "A5_2015 (Educación)", ylab = "Grupo de Estrés")