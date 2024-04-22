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
	prev, next *tVertex // pointers to the adjacent vertex
	index      int64    // Vertex index
	ear        bool     // ear is required to implement the triangulation algorithm
	coord      tPoint   // Coordinates, coord[X] & coord[Y]
}

// Add a new Vertex to the Polygon
func (polygon *tVertex) AddVertex(point tPoint) {
	if polygon == nil {
		fmt.Println("Empty polygon")
		return
	}

	vVertex := new(tVertex)

	vVertex.index = polygon.index
	polygon.index += 1
	vVertex.ear = false
	vVertex.coord = point

	vVertex.next = polygon.next
	vVertex.prev = polygon

	if polygon == polygon.next {
		polygon.prev = vVertex
	}
	polygon.next = vVertex
}

// Printing all vertexs of the poligon
func (polygon *tVertex) Print() {
	if polygon == nil {
		fmt.Println("Empty polygon")
		return
	}

	vVertex := polygon
	for {
		fmt.Printf("Vertex %3d (x:%f, y:%f)\n", vVertex.index, vVertex.coord[X], vVertex.coord[Y])
		vVertex = vVertex.next
		if vVertex == polygon {
			break
		}
	}
}

func main() {

	pPolygon := tVertex{nil, nil, 1, false, tPoint{rand.Float64(), rand.Float64()}}
	pPolygon.next = &pPolygon
	pPolygon.prev = &pPolygon

	for range 14 {
		pPolygon.AddVertex(tPoint{rand.Float64(), rand.Float64()})
	}

	pPolygon.Print()
}
