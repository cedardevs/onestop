package main

import (
	"github.com/spf13/viper"
	"testing"
	"github.com/rs/zerolog/log"
)

// 	layout1 := "2006-01-02"
// layout2 := "2003-01-02 00:00"
// layout3 := "January 2nd 2006 00:00:00"
// layout4 := "January 2nd 2006"
func TestParseStartAndEndTime(t *testing.T) {
	params1 := viper.New()
	params1.Set("stime", "2018-01-01")
	params1.Set("etime", "2019-01-01")

	params2 := viper.New()
	params2.Set("stime", "2018-01-01")

	params3 := viper.New()
	params3.Set("etime", "2019-01-01")

	params4 := viper.New()
	params4.Set("stime", "August 17 2019 14:37:01")
	// params4.Set("stime", "Aug 17, 2019 at 14:37pm (MST)")

	params5 := viper.New()
	params5.Set("stime", "Aug 17, 2019 at 2:38pm")

	paramList := []*viper.Viper{params1, params2, params3, params4, params5}

	expectedResult1 := []string{"{\"type\":\"datetime\", \"after\":\"2018-01-01T00:00:00Z\", \"before\":\"2019-01-01T00:00:00Z\"}"}
	expectedResult2 := []string{"{\"type\":\"datetime\", \"after\":\"2018-01-01T00:00:00Z\"}"}
	expectedResult3 := []string{"{\"type\":\"datetime\", \"before\":\"2019-01-01T00:00:00Z\"}"}
	expectedResult4 := []string{"{\"type\":\"datetime\", \"after\":\"2019-08-17T14:37:01Z\"}"}
	expectedResult5 := []string{"{\"type\":\"datetime\", \"after\":\"2019-08-17T14:38:00Z\"}"}

	expectedResults := [][]string{expectedResult1, expectedResult2, expectedResult3, expectedResult4, expectedResult5}

	for i := 1; i < len(expectedResults); i++ {
		got := parseStartAndEndTime(paramList[i])
		if got[0] != expectedResults[i][0] {
			log.Info().Msg(got[0])
			log.Info().Msg(expectedResults[i][0])
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

	expectedResult1 := []string{"{\"type\":\"datetime\", \"after\":\"2019-01-01T00:00:00Z\", \"before\":\"2019-01-02T00:00:00Z\"}"}
	expectedResult2 := []string{"{\"type\":\"datetime\", \"after\":\"2019-01-01T00:00:00Z\", \"before\":\"2019-01-02T00:00:00Z\"}"}

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
