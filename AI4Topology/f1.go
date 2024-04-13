// Copyright 2024 Andres Leonardo Martinez Ortiz
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package main

import (
	"fmt"
	"math/rand"
)

// Const definition X & Y (dimensions)
const (
	X   = iota // 0
	Y          // 1
	DIM        // 2
)

// Type tpoint (topological point) define as a bidimensional (X, Y)
// array of floats
type tPoint [DIM]float64

// tVertex is the define as a struct with the fields required for
// the implementation of a polygon as a circular list, including
// also some fields required for the triangulation algorithm
type tVertex struct {
	index       int64    // Vertex index
	coord       tPoint   // Coordinates, coord[X] & coord[Y]
	ear         bool     // ear is required to implement the triangulation algorithm
	left, right *tVertex // pointers to the adjacent vertex
}

// tPolygon is a pointer to the first vertex
type tPolygon *tVertex

func main() {
	var aPolygon tPolygon

	aPolygon = new(tVertex)
	aPolygon.index = 0
	aPolygon.ear = true
	aPolygon.left = aPolygon
	aPolygon.right = aPolygon
	aPolygon.coord[X] = rand.Float64()
	aPolygon.coord[Y] = rand.Float64()

	fmt.Printf("Vertex:\n\tX: %f\n\tY: %f\n", aPolygon.coord[X], aPolygon.coord[Y])
}
