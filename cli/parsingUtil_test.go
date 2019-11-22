package main

import (
	"github.com/rs/zerolog/log"
	"github.com/spf13/viper"
	"testing"
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

	params5 := viper.New()
	params5.Set("stime", "Aug 17, 2019 at 2:38pm")

	params6 := viper.New()
	params6.Set("stime", "March 31 2003 17:30")
	params6.Set("etime", "2003-04-01 10:32:49")

	//direct scdr-files examples
	params7 := viper.New()
	params7.Set("stime", "March 1st 2003")

	params8 := viper.New()
	params8.Set("stime", "March 2nd 2003")

	params9 := viper.New()
	params9.Set("stime", "March 3rd 2003")

	params10 := viper.New()
	params10.Set("stime", "March 4th 2003")

	params11 := viper.New()
	params11.Set("stime", "2003-04-01 10:32:49 PST")
	params11.Set("etime", "October 10th 2010 at 17:30")

	params12 := viper.New()
	params12.Set("stime", "Sun Oct 10 10:30:00 MST 2019")

	paramList := []*viper.Viper{params1, params2, params3, params4, params5, params6, params7, params8, params9, params10, params11, params12}

	expectedResults := [][]string{
		[]string{"{\"type\":\"datetime\", \"after\":\"2018-01-01T00:00:00Z\", \"before\":\"2019-01-01T00:00:00Z\"}"},
		[]string{"{\"type\":\"datetime\", \"after\":\"2018-01-01T00:00:00Z\"}"},
		[]string{"{\"type\":\"datetime\", \"before\":\"2019-01-01T00:00:00Z\"}"},
		[]string{"{\"type\":\"datetime\", \"after\":\"2019-08-17T14:37:01Z\"}"},
		[]string{"{\"type\":\"datetime\", \"after\":\"2019-08-17T14:38:00Z\"}"},
		[]string{"{\"type\":\"datetime\", \"after\":\"2003-03-31T17:30:00Z\", \"before\":\"2003-04-01T10:32:49Z\"}"},
		[]string{"{\"type\":\"datetime\", \"after\":\"2003-03-01T00:00:00Z\"}"},
		[]string{"{\"type\":\"datetime\", \"after\":\"2003-03-02T00:00:00Z\"}"},
		[]string{"{\"type\":\"datetime\", \"after\":\"2003-03-03T00:00:00Z\"}"},
		[]string{"{\"type\":\"datetime\", \"after\":\"2003-03-04T00:00:00Z\"}"},
		[]string{"{\"type\":\"datetime\", \"after\":\"2003-04-01T10:32:49Z\", \"before\":\"2010-10-10T17:30:00Z\"}"},
		[]string{"{\"type\":\"datetime\", \"after\":\"2019-10-10T17:30:00Z\"}"},
	}

	for i := 1; i < len(expectedResults); i++ {
		got := parseStartAndEndTime(paramList[i])
		if len(got) > 0 && got[0] != expectedResults[i][0] {
			log.Info().Msg(got[0])
			log.Info().Msg(expectedResults[i][0])
			t.Error("TestParseStartAndEndTime Failed")
		} else if len(got) == 0 {
			log.Info().Msg("Empty stime/etime result")
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

	params3 := viper.New()
	params3.Set("date", "01-01")

	paramList := []*viper.Viper{params1, params2, params3}

	expectedResult1 := []string{"{\"type\":\"datetime\", \"after\":\"2019-01-01T00:00:00Z\", \"before\":\"2019-01-02T00:00:00Z\"}"}
	expectedResult2 := []string{"{\"type\":\"datetime\", \"after\":\"2019-01-01T00:00:00Z\", \"before\":\"2019-01-02T00:00:00Z\"}"}
	expectedResult3 := []string{"{\"type\":\"datetime\", \"after\":\"2019-01-01T00:00:00Z\", \"before\":\"2019-01-02T00:00:00Z\"}"}

	expectedResults := [][]string{expectedResult1, expectedResult2, expectedResult3}
	for i := 1; i < len(expectedResults); i++ {
		got := parseDate(paramList[i])
		if got[0] != expectedResults[i][0] {
			log.Info().Msg(got[0])
			log.Info().Msg(expectedResults[i][0])
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
