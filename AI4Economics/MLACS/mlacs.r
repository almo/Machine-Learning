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

# Loading dependencies
if (!require(ggplot2))
  install.packages("ggplot2")
library(ggplot2)

# Loading dataset
NFCS2021 <- read.csv('Datasets/nfcs-2021.csv', stringsAsFactors = TRUE)

# Loading definition and refactoring of data frames
source('demographics.r')
source('capabilities.r')
source('stress.r')

#
# Preprocessing
#

# Missing values
# Demographics
DemographicsNA <- data.frame(
  Feature = character(),
  Missing_Count = numeric(),
  Missing_Percent = numeric(),
  stringsAsFactors = FALSE
)

for (feature in DemographicsFeatures) {
  missing_count <- sum(is.na(NFCS2021Demographics[[feature]]))
  missing_percent <- round((missing_count / nrow(NFCS2021Demographics)) * 100, 2)
  DemographicsNA[nrow(DemographicsNA) + 1, ] <- c(feature, missing_count, missing_percent)
}

ggplot(DemographicsNA, aes(x = Feature, y = Missing_Percent)) +
  geom_col(fill = "steelblue") +
  geom_text(aes(label = paste0(Missing_Percent, "%")), vjust = -0.5, size = 3) +
  labs(title = "Variables Demograficas - Porcentaje de Valores Faltantes", x = "Variable", y = "Porcentaje") +
  theme(axis.text.x = element_text(angle = 45, hjust = 1))

# Missing values
# Capabilities
CapabilitiesNA <- data.frame(
  Feature = character(),
  Missing_Count = numeric(),
  Missing_Percent = numeric(),
  stringsAsFactors = FALSE
)

for (feature in CapabilitiesFeatures) {
  missing_count <- sum(is.na(NFCS2021Capabilities[[feature]]))
  missing_percent <- round((missing_count / nrow(NFCS2021Capabilities)) * 100, 2)
  CapabilitiesNA[nrow(CapabilitiesNA) + 1, ] <- c(feature, missing_count, missing_percent)
}

ggplot(CapabilitiesNA, aes(x = Feature, y = Missing_Percent)) +
  geom_col(fill = "steelblue") +
  geom_text(aes(label = paste0(Missing_Percent, "%")), vjust = -0.5, size = 3) +
  labs(title = "Variables Alfabetización Financiera - Porcentaje de Valores Faltantes", x = "Variable", y = "Porcentaje") +
  theme(axis.text.x = element_text(angle = 45, hjust = 1))


# Missing values
# Stress Features
StressNA <- data.frame(
  Feature = character(),
  Missing_Count = numeric(),
  Missing_Percent = numeric(),
  stringsAsFactors = FALSE
)

for (feature in StressFeatures) {
  missing_count <- sum(is.na(NFCS2021Stress[[feature]]))
  missing_percent <- round((missing_count / nrow(NFCS2021Stress)) * 100, 2)
  StressNA[nrow(StressNA) + 1, ] <- c(feature, missing_count, missing_percent)
}

ggplot(StressNA, aes(x = Feature, y = Missing_Percent)) +
  geom_col(fill = "steelblue") +
  geom_text(
    aes(label = paste0(Missing_Percent, "%")),
    hjust = -0.1 ,
    vjust = -0.5,
    size = 3,
    angle = 45
  ) +
  labs(title = "Variables Estrés Financiero - Porcentaje de Valores Faltantes", x = "Variable", y = "Porcentaje") +
  theme(axis.text.x = element_text(angle = 45, hjust = 1))

