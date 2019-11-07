package main

import (
	"github.com/spf13/viper"
	"testing"
)

func TestParseStartAndEndTime(t *testing.T) {
	params1 := viper.New()
	params1.Set("stime", "2018/01/01")
	params1.Set("etime", "2019/01/01")

	params2 := viper.New()
	params2.Set("stime", "2018/01/01")

	params3 := viper.New()
	params3.Set("etime", "2019/01/01")

	paramList := []*viper.Viper{params1, params2, params3}

	expectedResult1 := []string{"{\"type\":\"datetime\", \"relation\": \"within\", \"after\":\"2018-01-01T00:00:00Z\", \"before\":\"2019-01-01T00:00:00Z\"}"}
	expectedResult2 := []string{"{\"type\":\"datetime\", \"relation\": \"within\", \"after\":\"2018-01-01T00:00:00Z\"}"}
	expectedResult3 := []string{"{\"type\":\"datetime\", \"relation\": \"within\", \"before\":\"2019-01-01T00:00:00Z\"}"}

	expectedResults := [][]string{expectedResult1, expectedResult2, expectedResult3}

	for i := 1; i < len(expectedResults); i++ {
		got := parseStartAndEndTime(paramList[i])
		if got[0] != expectedResults[i][0] {
			t.Error("TestParseStartAndEndTime Failed")
		}
	}
}

func TestParseDateTime(t *testing.T) {
	params1 := viper.New()
	params1.Set("date", "2019/01/01")

	params2 := viper.New()
	params2.Set("date", "01/01")

	paramList := []*viper.Viper{params1, params2}

	expectedResult1 := []string{"{\"type\":\"datetime\", \"relation\": \"within\", \"after\":\"2019-01-01T00:00:00Z\", \"before\":\"2019-01-02T00:00:00Z\"}"}
	expectedResult2 := []string{"{\"type\":\"datetime\", \"relation\": \"within\", \"after\":\"2019-01-01T00:00:00Z\", \"before\":\"2019-01-02T00:00:00Z\"}"}

	expectedResults := [][]string{expectedResult1, expectedResult2}
	for i := 1; i < len(expectedResults); i++ {
		got := parseDate(paramList[i])
		if got[0] != expectedResults[i][0] {
			t.Error("TestParseDateTime Failed")
		}
	}
}

func TestParsePolygon(t *testing.T) {
	params := viper.New()
	params.Set("area", "POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))")
	expectedResult := []string{"{\"geometry\": { \"coordinates\": [[[22.686768,34.051522], [30.606537,34.051522], [30.606537,41.280903], [22.686768,41.280903], [22.686768,34.051522]]], \"type\": \"Polygon\"}, \"type\": \"geometry\"}"}

	got := parsePolygon(params)
	if got[0] != expectedResult[0] {
		t.Error("TestParsePolygon Failed")
	}
}
