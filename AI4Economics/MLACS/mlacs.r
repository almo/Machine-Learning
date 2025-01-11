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

# Loading dataset
NFCS2021 <- read.csv('Datasets/nfcs-2021.csv', stringsAsFactors = TRUE)

#
# Splitting dataset in the subsets: demographics, financial capabilities and 
# financial stress
#
# Demographics Features
#
DemographicsFeatures <- c("NFCSID","STATEQ","A50A","A3Ar_w","A50B","A5_2015","A6",
                          "A7","A11","A8_2021","A9","A40","A10","A21_2015","A41")
str(DemographicsFeatures)

NFCS2021Demographics <- NFCS2021[,DemographicsFeatures]
str(NFCS2021Demographics)

#
# Financial Capabilities Features
#
CapabilitiesFeatures <- c('NFCSID','B1','B2','B31','B42','B43','C1_2012','C2_2012',
                          'C5_2012','B14','EA_1','E7','F1','H1','M1_1','M4','M20')
str(CapabilitiesFeatures)
NFCS2021Capabilities <- NFCS2021[,CapabilitiesFeatures]
str(CapabilitiesFeatures)

#
# Financial Stress Features
#
StressFeatures <- c('NFCSID','J1','J3','J4','J5','J6','J10','J20','J32',
                    'C10_2012','E15_2015','F2_1','F2_2','F2_3','F2_4','F2_5',
                    'F2_6','P50','G20','G35','G38','H30_1','H30_2','H30_3')
str(StressFeatures)
NFCS2021Stress <- NFCS2021[,StressFeatures]
str(NFCS2021Stress)
