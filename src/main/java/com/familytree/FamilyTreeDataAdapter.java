package com.familytree;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.Collection; // Added for clarity
import java.util.stream.Collectors;

/**
 * Custom TypeAdapter for FamilyTreeData to handle the deserialization of
 * the ObservableList and Map fields, which are declared as final.
 * This ensures Gson does not attempt to create new instances of these fields,
 * which would cause an IllegalArgumentException.
 */
public class FamilyTreeDataAdapter implements JsonSerializer<FamilyTreeData>, JsonDeserializer<FamilyTreeData> {

    @Override
    public FamilyTreeData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Create a new instance of FamilyTreeData.
        FamilyTreeData data = FamilyTreeData.getInstance();
        JsonObject jsonObject = json.getAsJsonObject();

        // Deserialize 'people' into a temporary map and then populate the final map.
        // We use TypeToken to properly deserialize the map with UUID keys.
        Type peopleMapType = new TypeToken<Map<String, Person>>() {}.getType();
        Map<String, Person> people = context.deserialize(jsonObject.get("people"), peopleMapType);
        data.getPeople().clear();
        data.getPeople().putAll(people);

        // Deserialize 'layoutPositions' in the same way.
        Type layoutPositionsMapType = new TypeToken<Map<String, Position>>() {}.getType();
        Map<String, Position> layoutPositions = context.deserialize(jsonObject.get("layoutPositions"), layoutPositionsMapType);
        data.getLayoutPositions().clear();
        data.getLayoutPositions().putAll(layoutPositions);

        // Deserialize 'personList' into a temporary list and then populate the final ObservableList.
        JsonArray personListJson = jsonObject.getAsJsonArray("personList");
        data.getPersonList().clear(); // Clear the existing ObservableList
        for (JsonElement element : personListJson) {
            Person person = context.deserialize(element, Person.class);
            data.getPersonList().add(person);
        }

        return data;
    }

    @Override
    public JsonElement serialize(FamilyTreeData src, Type typeOfSrc, JsonSerializationContext context) {
        // For serialization, we can just use the default Gson behavior.
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("people", context.serialize(src.getAllPeople().stream().collect(Collectors.toMap(Person::getId, person -> person))));
        jsonObject.add("layoutPositions", context.serialize(src.getLayoutPositions()));
        jsonObject.add("personList", context.serialize(src.getPersonList()));
        return jsonObject;
    }
}
