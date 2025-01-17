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
# Preprocessing
#

# Missing values
# Demographics
NA.Demographics <- data.frame(
  Feature = character(),
  Missing_Count = numeric(),
  Missing_Percent = numeric(),
  stringsAsFactors = FALSE
)

for (feature in DemographicsFeatures) {
  missing_count <- sum(is.na(NFCS.2021.Demographics[[feature]]))
  missing_percent <- round((missing_count / nrow(NFCS.2021.Demographics)) * 100, 2)
  nr <- nrow(NA.Demographics) + 1
  NA.Demographics[nr, "Feature"] <- feature
  NA.Demographics[nr, "Missing_Count"] <- missing_count
  NA.Demographics[nr, "Missing_Percent"] <- missing_percent
  
}

ggplot(NA.Demographics, aes(x = Feature, y = Missing_Percent)) +
  geom_col(fill = "steelblue") +
  geom_text(aes(label = paste0(Missing_Percent, "%")), vjust = -0.5, size = 3) +
  labs(title = "Variables Demográficas - Porcentaje de Valores Faltantes", x = "Variable", y = "Porcentaje") +
  theme(axis.text.x = element_text(angle = 45, hjust = 1))

print("Demographics features: missing values for A10")
print(table(NFCS.2021.Demographics$A10, useNA = "ifany"))

print("Demographics features: missing values for A21_2015")
print(table(NFCS.2021.Demographics$A21_2015, useNA = "ifany"))

# Missing values
# Capabilities
NA.Capabilities <- data.frame(
  Feature = character(),
  Missing_Count = numeric(),
  Missing_Percent = numeric(),
  stringsAsFactors = FALSE
)

for (feature in CapabilitiesFeatures) {
  missing_count <- sum(is.na(NFCS.2021.Capabilities[[feature]]))
  missing_percent <- round((missing_count / nrow(NFCS.2021.Capabilities)) * 100, 2)
  nr <- nrow(NA.Capabilities) + 1
  NA.Capabilities[nr, "Feature"] <- feature
  NA.Capabilities[nr, "Missing_Count"] <- missing_count
  NA.Capabilities[nr, "Missing_Percent"] <- missing_percent
}

ggplot(NA.Capabilities, aes(x = Feature, y = Missing_Percent)) +
  geom_col(fill = "steelblue") +
  geom_text(aes(label = paste0(Missing_Percent, "%")), vjust = -0.5, size = 3) +
  labs(title = "Variables Alfabetización Financiera - Porcentaje de Valores Faltantes", x = "Variable", y = "Porcentaje") +
  theme(axis.text.x = element_text(angle = 45, hjust = 1))

print("Capabilities features: missing values for C2_2012")
print(table(NFCS.2021.Capabilities$C2_2012, useNA = "ifany"))

print("Capabilities features: missing values for C5_2012")
print(table(NFCS.2021.Capabilities$C5_2012, useNA = "ifany"))

print("Capabilities features: missing values for E7")
print(table(NFCS.2021.Capabilities$E7, useNA = "ifany"))

print("Capabilities features: missing values for B14")
print(table(NFCS.2021.Capabilities$B14, useNA = "ifany"))

# Missing values
# Stress Features
NA.Stress <- data.frame(
  Feature = character(),
  Missing_Count = numeric(),
  Missing_Percent = numeric(),
  stringsAsFactors = FALSE
)

for (feature in StressFeatures) {
  missing_count <- sum(is.na(NFCS.2021.Stress[[feature]]))
  missing_percent <- round((missing_count / nrow(NFCS.2021.Stress)) * 100, 2)
  nr <- nrow(NA.Stress) + 1
  NA.Stress[nr, "Feature"] <- feature
  NA.Stress[nr, "Missing_Count"] <- missing_count
  NA.Stress[nr, "Missing_Percent"] <- missing_percent
}

ggplot(NA.Stress, aes(x = Feature, y = Missing_Percent)) +
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

print("Stress features: missing values for C10_2012")
print(table(NFCS.2021.Stress$C10_2012, useNA = "ifany"))

print("Stress features: missing values for E15_2015")
print(table(NFCS.2021.Stress$E15_2015, useNA = "ifany"))

print("Stress features: missing values for G35")
print(table(NFCS.2021.Stress$G35, useNA = "ifany"))

print("Stress features: missing values for J6")
print(table(NFCS.2021.Stress$J6, useNA = "ifany"))

print("Stress features: missing values for F2_1")
print(table(NFCS.2021.Stress$F2_1, useNA = "ifany"))

print("Stress features: missing values for F2_2")
print(table(NFCS.2021.Stress$F2_2, useNA = "ifany"))

print("Stress features: missing values for F2_3")
print(table(NFCS.2021.Stress$F2_3, useNA = "ifany"))

print("Stress features: missing values for F2_4")
print(table(NFCS.2021.Stress$F2_4, useNA = "ifany"))

print("Stress features: missing values for F2_5")
print(table(NFCS.2021.Stress$F2_5, useNA = "ifany"))

print("Stress features: missing values for F2_6")
print(table(NFCS.2021.Stress$F2_6, useNA = "ifany"))
