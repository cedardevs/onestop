package parse

import (
	"github.com/cedardevs/onestop/cli/internal/pkg/flags"
	"github.com/rs/zerolog/log"
	"github.com/spf13/viper"
	"strconv"
	"testing"
	"time"
)

func TestParseStartAndEndTime(t *testing.T) {

	params1 := viper.New()
	params1.Set(flags.StartTimeScdrFlag, "2018-01-01")
	params1.Set(flags.EndTimeScdrFlag, "2019-01-01")
	expectedResult1 := []string{"{\"type\":\"datetime\", \"after\":\"2018-01-01T00:00:00Z\", \"before\":\"2019-01-01T00:00:00Z\"}"}

	params2 := viper.New()
	params2.Set(flags.StartTimeScdrFlag, "2018-01-01")
	expectedResult2 := []string{"{\"type\":\"datetime\", \"after\":\"2018-01-01T00:00:00Z\"}"}

	params3 := viper.New()
	params3.Set(flags.EndTimeScdrFlag, "2019-01-01")
	expectedResult3 := []string{"{\"type\":\"datetime\", \"before\":\"2019-01-01T00:00:00Z\"}"}

	params4 := viper.New()
	params4.Set(flags.StartTimeScdrFlag, "August 17 2019 14:37:01")
	expectedResult4 := []string{"{\"type\":\"datetime\", \"after\":\"2019-08-17T14:37:01Z\"}"}

	params5 := viper.New()
	params5.Set(flags.StartTimeScdrFlag, "Aug 17, 2019 at 2:38pm")
	expectedResult5 := []string{"{\"type\":\"datetime\", \"after\":\"2019-08-17T14:38:00Z\"}"}

	params6 := viper.New()
	params6.Set(flags.StartTimeScdrFlag, "March 31 2003 17:30")
	params6.Set(flags.EndTimeScdrFlag, "2003-04-01 10:32:49")
	expectedResult6 := []string{"{\"type\":\"datetime\", \"after\":\"2003-03-31T17:30:00Z\", \"before\":\"2003-04-01T10:32:49Z\"}"}

	//direct scdr-files examples
	params7 := viper.New()
	params7.Set(flags.StartTimeScdrFlag, "March 1st 2003")
	expectedResult7 := []string{"{\"type\":\"datetime\", \"after\":\"2003-03-01T00:00:00Z\"}"}

	params8 := viper.New()
	params8.Set(flags.StartTimeScdrFlag, "March 2nd 2003")
	expectedResult8 := []string{"{\"type\":\"datetime\", \"after\":\"2003-03-02T00:00:00Z\"}"}

	params9 := viper.New()
	params9.Set(flags.StartTimeScdrFlag, "March 3rd 2003")
	expectedResult9 := []string{"{\"type\":\"datetime\", \"after\":\"2003-03-03T00:00:00Z\"}"}

	params10 := viper.New()
	params10.Set(flags.StartTimeScdrFlag, "March 4th 2003")
	expectedResult10 := []string{"{\"type\":\"datetime\", \"after\":\"2003-03-04T00:00:00Z\"}"}

	params11 := viper.New()
	params11.Set(flags.StartTimeScdrFlag, "2003-04-01 10:32:49 PST")
	params11.Set(flags.EndTimeScdrFlag, "October 10th 2010 at 17:30")
	expectedResult11 := []string{"{\"type\":\"datetime\", \"after\":\"2003-04-01T10:32:49Z\", \"before\":\"2010-10-10T17:30:00Z\"}"}

	params12 := viper.New()
	params12.Set(flags.StartTimeScdrFlag, "Sun Oct 10 10:30:00 MST 2019")
	expectedResult12 := []string{"{\"type\":\"datetime\", \"after\":\"2019-10-10T17:30:00Z\"}"}

	paramList := []*viper.Viper{params1, params2, params3, params4, params5, params6, params7, params8, params9, params10, params11, params12}

	expectedResults := [][]string{
		expectedResult1,
		expectedResult2,
		expectedResult3,
		expectedResult4,
		expectedResult5,
		expectedResult6,
		expectedResult7,
		expectedResult8,
		expectedResult9,
		expectedResult10,
		expectedResult11,
		expectedResult12,
	}

	for i := 1; i < len(expectedResults); i++ {
		got := ParseStartAndEndTime(paramList[i])
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
	params1.Set(flags.DateFilterFlag, "2019/01/01")

	params2 := viper.New()
	params2.Set(flags.DateFilterFlag, "01/01")

	params3 := viper.New()
	params3.Set(flags.DateFilterFlag, "01-01")

	paramList := []*viper.Viper{params1, params2, params3}

	currentYear := strconv.Itoa(time.Now().Year())
	expectedResult1 := []string{"{\"type\":\"datetime\", \"after\":\"" + currentYear + "-01-01T00:00:00Z\", \"before\":\"" + currentYear + "-01-02T00:00:00Z\"}"}
	expectedResult2 := []string{"{\"type\":\"datetime\", \"after\":\"" + currentYear + "-01-01T00:00:00Z\", \"before\":\"" + currentYear + "-01-02T00:00:00Z\"}"}
	expectedResult3 := []string{"{\"type\":\"datetime\", \"after\":\"" + currentYear + "-01-01T00:00:00Z\", \"before\":\"" + currentYear + "-01-02T00:00:00Z\"}"}

	expectedResults := [][]string{expectedResult1, expectedResult2, expectedResult3}
	for i := 1; i < len(expectedResults); i++ {
		got := ParseDate(paramList[i])
		if got[0] != expectedResults[i][0] {
			log.Info().Msg("GOT")
			log.Info().Msg(got[0])
			log.Info().Msg("EXPECTED")
			log.Info().Msg(expectedResults[i][0])
			t.Error("TestParseDateTime Failed")
		}
	}
}

func TestParseSince(t *testing.T) {
	params1 := viper.New()
	params1.Set(flags.SinceFlag, "2020-01-31T12:02:28")
	expectedResult1 := []string{"{\"type\":\"queryText\", \"value\":\"stagedDate:>1582282512000\"}"}
	// {"type":"queryText", "value":"stagedDate:>1580428948105"}
	params2 := viper.New()
	params2.Set(flags.SinceFlag, "2020-02-21T10:55:12")
	expectedResult2 := []string{"{\"type\":\"queryText\", \"value\":\"stagedDate:>1582282512000\"}"}

	paramList := []*viper.Viper{params1, params2}

	expectedResults := [][]string{expectedResult1, expectedResult2}
	for i := 1; i < len(expectedResults); i++ {
		got := ParseSince(paramList[i])
		if got[0] != expectedResults[i][0] {
			log.Info().Msg("GOT")
			log.Info().Msg(got[0])
			log.Info().Msg("EXPECTED")
			log.Info().Msg(expectedResults[i][0])
			t.Error("TestParseSince Failed")
		}
	}
}

func TestParseMonth(t *testing.T) {
	params := viper.New()
	params.Set(flags.MonthFlag, "12")
	expectedResult := []string{"{\"type\":\"queryText\", \"value\":\"beginMonth:12\"}"}
	got := ParseMonth(params)
	if got[0] != expectedResult[0] {
		log.Info().Msg(got[0])
		log.Info().Msg(expectedResult[0])
		t.Error("TestParseMonth Failed")
	}
}

func TestParseDayOfMonth(t *testing.T) {
	params := viper.New()
	params.Set(flags.DayFlag, "15")
	expectedResult := []string{"{\"type\":\"queryText\", \"value\":\"beginDayOfMonth:15\"}"}
	got := ParseDayOfMonth(params)
	if got[0] != expectedResult[0] {
		log.Info().Msg(got[0])
		log.Info().Msg(expectedResult[0])
		t.Error("TestParseDayOfMonth Failed")
	}
}

func TestParseDayOfYear(t *testing.T) {
	params := viper.New()
	params.Set(flags.DoyFlag, "300")
	expectedResult := []string{"{\"type\":\"queryText\", \"value\":\"beginDayOfYear:300\"}"}
	got := ParseDayOfYear(params)
	if got[0] != expectedResult[0] {
		log.Info().Msg(got[0])
		log.Info().Msg(expectedResult[0])
		t.Error("TestParseDayOfYear Failed")
	}
}

func TestParseYear(t *testing.T) {
	params := viper.New()
	params.Set(flags.YearFlag, "2018")
	expectedResult := []string{"{\"type\":\"datetime\", \"after\":\"2018-01-01T00:00:00Z\", \"before\":\"2019-01-01T00:00:00Z\"}"}
	got := ParseYear(params)
	if got[0] != expectedResult[0] {
		log.Info().Msg(got[0])
		log.Info().Msg(expectedResult[0])
		t.Error("TestParseYear Failed")
	}

}

func TestParsePolygon(t *testing.T) {
	params := viper.New()
	params.Set(flags.SpatialFilterFlag, "POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))")
	expectedResult := []string{"{\"geometry\": { \"coordinates\": [[[22.686768,34.051522], [30.606537,34.051522], [30.606537,41.280903], [22.686768,41.280903], [22.686768,34.051522]]], \"type\": \"Polygon\"}, \"type\": \"geometry\"}"}

	got := ParsePolygon(params)
	if got[0] != expectedResult[0] {
		t.Error("TestParsePolygon Failed")
	}
}
