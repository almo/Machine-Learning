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
package ai4topology

import "fmt"

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

// tPolygon encapsulates the pointer to the first vertex
type tPolygon struct {
	head *tVertex
}

// Add a new vertex to the polygon
func (polygon *tPolygon) Addvertex(point tPoint) {
	if polygon == nil {
		fmt.Println("Polygon is nil")
		return
	}

	vVertex := new(tVertex)
	if polygon.head == nil {
		polygon.head = vVertex
	}

	vVertex.index = polygon.head.index
	polygon.head.index += 1
	vVertex.ear = false
	vVertex.coord = point

	vVertex.next = polygon.head.next
	vVertex.prev = polygon.head

	if polygon.head == polygon.head.next {
		polygon.head.prev = vVertex
	}
	polygon.head.next = vVertex
}

// printing polygon vertexes
func (polygon *tPolygon) Print() {
	if polygon == nil {
		fmt.Println("Polygon is nil")
		return
	}

	if polygon.head == nil {
		fmt.Println("Empty polygon")
		return
	}

	vVertex := polygon.head
	for {
		fmt.Printf("Vertex %3d (x:%f, y:%f)\n", vVertex.index, vVertex.coord[X], vVertex.coord[Y])
		vVertex = vVertex.next
		if vVertex == polygon.head {
			break
		}
	}

}
