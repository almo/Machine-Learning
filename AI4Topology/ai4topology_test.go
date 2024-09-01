package ai4topology

import (
	"math/rand"
	"testing"
)

func TestPrintingPolygon(t *testing.T) {
	auxPolygon := tPolygon{nil}

	for range 14 {
		auxPolygon.Addvertex(tPoint{rand.Float64(), rand.Float64()})
	}

	auxPolygon.Print()
}

func ExampleMain() {

	auxPolygon := tPolygon{nil}

	for range 14 {
		auxPolygon.Addvertex(tPoint{rand.Float64(), rand.Float64()})
	}

	auxPolygon.Print()
}
