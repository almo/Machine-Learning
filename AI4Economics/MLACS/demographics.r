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
# Splitting dataset in the subsets: demographics, financial capabilities and
# financial stress
#
# Demographics Features
#
DemographicsFeatures <- c(
  "NFCSID",
  "STATEQ",
  "A50A",
  "A3Ar_w",
  "A50B",
  "A5_2015",
  "A6",
  "A7",
  "A11",
  "A8_2021",
  "A9",
  "A40",
  "A10",
  "A21_2015",
  "A41"
)
str(DemographicsFeatures)

NFCS.2021.Demographics <- NFCS.2021[, DemographicsFeatures]
str(NFCS.2021.Demographics)

#
# Refactoring the datasets
#
#
# Demographics Features
#
# Convert STATEQ to a factor
# State
NFCS.2021.Demographics$STATEQ <- factor(
  NFCS.2021.Demographics$STATEQ,
  levels = c(
    1,
    2,
    3,
    4,
    5,
    6,
    7,
    8,
    9,
    10,
    11,
    12,
    13,
    14,
    15,
    16,
    17,
    18,
    19,
    20,
    21,
    22,
    23,
    24,
    25,
    26,
    27,
    28,
    29,
    30,
    31,
    32,
    33,
    34,
    35,
    36,
    37,
    38,
    39,
    40,
    41,
    42,
    43,
    44,
    45,
    46,
    47,
    48,
    49,
    50,
    51
  ),
  labels = c(
    'Alabama',
    'Alaska',
    'Arizona',
    'Arkansas',
    'California',
    'Colorado',
    'Connecticut',
    'Delaware',
    'District of Columbia',
    'Florida',
    'Georgia',
    'Hawaii',
    'Idaho',
    'Illinois',
    'Indiana',
    'Iowa',
    'Kansas',
    'Kentucky',
    'Louisiana',
    'Maine',
    'Maryland',
    'Massachusetts',
    'Michigan',
    'Minnesota',
    'Mississippi',
    'Missouri',
    'Montana',
    'Nebraska',
    'Nevada',
    'New Hampshire',
    'New Jersey',
    'New Mexico',
    'New York',
    'North Carolina',
    'North Dakota',
    'Ohio',
    'Oklahoma',
    'Oregon',
    'Pennsylvania',
    'Rhode Island',
    'South Carolina',
    'South Dakota',
    'Tennessee',
    'Texas',
    'Utah',
    'Vermont',
    'Virginia',
    'Washington',
    'West Virginia',
    'Wisconsin',
    'Wyoming'
  )
)

# Convert A50A to a factor
# [GENDER (non-binary randomly assigned)]
NFCS.2021.Demographics$A50A <- factor(
  NFCS.2021.Demographics$A50A,
  levels = c(1, 2),
  labels = c("Male", "Female")
)


# Convert A3Ar_w to a factor
# Age group
NFCS.2021.Demographics$A3Ar_w <- factor(
  NFCS.2021.Demographics$A3Ar_w,
  levels = c(1, 2, 3, 4, 5, 6),
  labels = c("18-24", "25-34", "35-44", "45-54", "55-64", "65+")
)

# Convert A50B to a factor
# GENDER/AGE NET (non-binary randomly assigned)
NFCS.2021.Demographics$A50B <- factor(
  NFCS.2021.Demographics$A50B,
  levels = c(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
  labels = c(
    "Male 18-24",
    "Male 25-34",
    "Male 35-44",
    "Male 45-54",
    "Male 55-64",
    "Male 65+",
    "Female 18-24",
    "Female 25-34",
    "Female 35-44",
    "Female 45-54",
    "Female 55-64",
    "Female 65+"
  )
)

# Convert A5_2015 to a factor
# What was the highest level of education that you completed? [2015 codes]
NFCS.2021.Demographics$A5_2015 <- factor(
  NFCS.2021.Demographics$A5_2015,
  levels = c(1, 2, 3, 4, 5, 6, 7, 99),
  labels = c(
    "Did not complete high school",
    "High school graduate - regular high school diploma",
    "High school graduate - GED or alternative credential",
    "Some college, no degree",
    "Associate's degree",
    "Bachelor's degree",
    "Post graduate degree",
    "Prefer not to say"
  )
)

# Convert A6 to a factor
# What is your marital status?
NFCS.2021.Demographics$A6 <- factor(
  NFCS.2021.Demographics$A6,
  levels = c(1, 2, 3, 4, 5, 99),
  labels = c(
    "Married",
    "Single",
    "Separated",
    "Divorced",
    "Widowed/widower",
    "Prefer not to say"
  )
)

# Convert A7 to a factor
# Which of the following describes your current living arrangements?
NFCS.2021.Demographics$A7 <- factor(
  NFCS.2021.Demographics$A7,
  levels = c(1, 2, 3, 4, 99),
  labels = c(
    "I am the only adult in the household",
    "I live with my spouse/partner/significant other",
    "I live in my parents' home",
    "I live with other family, friends, or roommates",
    "Prefer not to say"
  )
)


# Convert A11 to a factor
# How many children do you have who are financially dependent on you [or your
# spouse/partner]?
NFCS.2021.Demographics$A11 <- factor(
  NFCS.2021.Demographics$A11,
  levels = c(1, 2, 3, 4, 5, 6, 99),
  labels = c(
    "1",
    "2",
    "3",
    "4 or more",
    "No financially dependent children",
    "Do not have any children",
    "Prefer not to say"
  )
)

# Convert A8_2021 to a factor
# What is your [household's] approximate annual income, including wages, tips,
# investment income, public assistance, income from retirement plans, etc.? [2021 codes]
NFCS.2021.Demographics$A8_2021 <- factor(
  NFCS.2021.Demographics$A8_2021,
  levels = c(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 98, 99),
  labels = c(
    "Less than $15,000",
    "At least $15,000 but less than $25,000",
    "At least $25,000 but less than $35,000",
    "At least $35,000 but less than $50,000",
    "At least $50,000 but less than $75,000",
    "At least $75,000 but less than $100,000",
    "At least $100,000 but less than $150,000",
    "At least $150,000 but less than $200,000",
    "At least $200,000 but less than $300,000",
    "$300,000 or more",
    "Don't know",
    "Prefer not to say"
  )
)

# Convert A9 to a factor
# Which of the following best describes your current employment or work status?
NFCS.2021.Demographics$A9 <- factor(
  NFCS.2021.Demographics$A9,
  levels = c(1, 2, 3, 4, 5, 6, 7, 8, 99),
  labels = c(
    "Self-employed",
    "Work full-time for an employer [or the military]",
    "Work part-time for an employer [or the military]",
    "Homemaker",
    "Full-time student",
    "Permanently sick, disabled, or unable to work",
    "Unemployed or temporarily laid off",
    "Retired",
    "Prefer not to say"
  )
)

# Convert A40 to a factor
# [In addition to your main employment, did you also do other/Did you do any]
# work for pay in the past 12 months?
NFCS.2021.Demographics$A40 <- factor(
  NFCS.2021.Demographics$A40,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)

# Convert A10 to a factor
# Which of the following best describes your [spouse's/partner's] current
# employment or work status?
NFCS.2021.Demographics$A10 <- factor(
  NFCS.2021.Demographics$A10,
  levels = c(1, 2, 3, 4, 5, 6, 7, 8, 99),
  labels = c(
    "Self-employed",
    "Work full-time for an employer",
    "Work part-time for an employer",
    "Homemaker",
    "Full-time student",
    "Permanently sick, disabled, or unable to work",
    "Unemployed or temporarily laid off",
    "Retired",
    "Prefer not to say"
  )
)

# Convert A21_2015 to a factor
# Are you a part-time student taking courses for credit? [2015 base]
NFCS.2021.Demographics$A21_2015 <- factor(
  NFCS.2021.Demographics$A21_2015,
  levels = c(1, 2, 98, 99),
  labels = c("Yes", "No", "Don't know", "Prefer not to say")
)


# Convert A41 to a factor
# What was the highest level of education completed by the person or any of the
# people who raised you?
NFCS.2021.Demographics$A41 <- factor(
  NFCS.2021.Demographics$A41,
  levels = c(1, 2, 3, 4, 5, 6, 98, 99),
  labels = c(
    "Did not complete high school",
    "High school graduate/GED",
    "Some college, no degree",
    "Associate's degree",
    "Bachelor's degree",
    "Post graduate degree",
    "Don't know",
    "Prefer not to say"
  )
)
