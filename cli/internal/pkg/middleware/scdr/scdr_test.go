package middleware

import (
	"testing"
	"reflect"
	"github.com/rs/zerolog/log"
)

func TestFindGaps(t *testing.T) {
	intervalSeconds := "1800s" //half hour
	mockType := "ABI-L1b-Rad"
	mockRange1 := map[string]interface{}{"beginDate": "2019-10-07T09:10:29.000Z", "endDate": "2019-10-07T09:20:00.000Z"}
  mockRange2 := map[string]interface{}{"beginDate": "2019-10-07T12:20:29.000Z", "endDate": "2019-10-07T12:30:00.000Z"}
	mockRange3 := map[string]interface{}{"beginDate": "2019-10-07T13:00:29.000Z", "endDate": "2019-10-07T13:10:00.000Z"}
	mockRange4 := map[string]interface{}{"beginDate": "2019-10-07T13:10:01.000Z", "endDate": "2019-10-07T13:20:00.000Z"}
	mockRange5 := map[string]interface{}{"beginDate": "2019-10-07T14:20:00.000Z"}

	mockItem1 := map[string]interface{}{"id": "a", "attributes": mockRange1}
	mockItem2 := map[string]interface{}{"id": "b", "attributes": mockRange2}
	mockItem3 := map[string]interface{}{"id": "c", "attributes": mockRange3}
	mockItem4 := map[string]interface{}{"id": "d", "attributes": mockRange4}
	mockItem5 := map[string]interface{}{"id": "d", "attributes": mockRange5}

	mockItemList := []interface{}{mockItem1, mockItem2, mockItem3, mockItem4, mockItem5}
	expectedScdrResponse := []string{
		"Data collection: " + mockType,
		"Gap Start Time           | Gap End Time             | Gap Duration",
		"-------------------------+--------------------------+-------------",
		"2019-10-07T09:20:00.000Z | 2019-10-07T12:20:29.000Z | 3h0m29s",
		"2019-10-07T12:30:00.000Z | 2019-10-07T13:00:29.000Z | 30m29s",
	}

	result := FindGaps(mockType, intervalSeconds, mockItemList)

	eq := reflect.DeepEqual(expectedScdrResponse, result)

	if !eq {
		log.Info().Msg("GOT")
		for _,  v := range result{
			log.Info().Msg(v)
		}
		log.Info().Msg("Exepected")
		for _,  v := range expectedScdrResponse{
			log.Info().Msg(v)
		}
		t.Error("FindGaps FAILED")
	}
}
