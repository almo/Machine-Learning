/*
Copyright 2024 Andres Leonardo Martinez Ortiz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
package main
*/
package main

import (
	"fmt"
	"math/rand"
)

/**
 * Const definition X & Y (dimensions)
 */
const (
	X   = iota /* 0 */
	Y          /* 1 */
	DIM        /* 2 */
)

/**
 * Type tpoint (topological point) define as a bidimensional (X, Y)
 * array of floats
 */
type tpoint [DIM]float64

func main() {
	var p1 = tpoint{rand.Float64(), rand.Float64()}
	fmt.Printf("Point:\n\tX: %f\n\tY: %f\n", p1[X], p1[Y])
}
