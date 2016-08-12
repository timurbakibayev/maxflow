package com.gii.maxflow;

/**
 * Created by Timur on 24-Oct-15.
 * Parse.com Cloud
 */
public class Cloud {

    /*
    boolean initialized = false;
    public Cloud(android.content.Context context) {
        //Parse.enableLocalDatastore(context);
        //Parse.initialize(context, "LA6xyNMRinpKE1DZFioX4dre1ASgmoUYEEyCtnbo", "bh4xk7MVuQwEHDAXguzxqSBFDv4Vj4MGhAQyyz6x");

        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.put("color", 1);
        //testObject.saveInBackground();
        testObject.saveEventually(); //<-Good for cases when user does not have an internet connection

        initialized = true;
    }

    public void downloadCircles(final ArrayList<Circle> circle, final Properties properties) {
        if (initialized) {
            if (properties.firebaseUserEmail.equals(""))
                return;
            ParseQuery<ParseObject> query = ParseQuery.getQuery("TestCircle");
            query.setLimit(10000);
            query.whereEqualTo("username", properties.firebaseUserEmail);
            query.whereEqualTo("filename", properties.fileName);
            query.whereGreaterThan("updatedAt",properties.lastCloudUpdateCircle);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> parseCircle, ParseException e) {
                    if (e == null) {
                        syncCircleList(parseCircle, circle, properties);
                        Log.e("TestCircle", "Downloaded " + parseCircle.size() + " circles");
                        for (ParseObject _circle : parseCircle) {
                            Log.e("TestCircle", _circle.getString("name"));
                        }
                    } else {
                        Log.d("TestCircle", "Error: " + e.getMessage());
                    }
                }
            });
        }
    }

    public void downloadOperations(final ArrayList<com.gi123i.maxflow.Operation> operation, final Properties properties) {
        if (initialized) {
            if (properties.firebaseUserEmail.equals(""))
                return;
            ParseQuery<ParseObject> query = ParseQuery.getQuery("TestOperation");
            query.setLimit(10000);
            query.whereEqualTo("username", properties.firebaseUserEmail);
            query.whereEqualTo("filename", properties.fileName);
            query.whereGreaterThan("updatedAt",properties.lastCloudUpdateOperation);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> parseOperations, ParseException e) {
                    if (e == null) {
                        syncOperationList(parseOperations, operation, properties);
                        Log.e("TestCircle", "Downloaded " + parseOperations.size() + " operations");
                    } else {
                        Log.d("TestCircle", "Error: " + e.getMessage());
                    }
                }
            });
        }
    }

    public void syncOperationList(List<ParseObject> parseOperationList, ArrayList<com.gi123i.maxflow.Operation> operation, Properties properties) {
        int j = 0;
        for (int i = 0; i < parseOperationList.size(); i++) {
            ParseObject parseOperation = parseOperationList.get(i);
            if (parseOperation.getUpdatedAt().compareTo(properties.lastCloudUpdateOperation) > 0)
                properties.lastCloudUpdateOperation = parseOperation.getUpdatedAt();
            String id = parseOperation.getString("operation_id");
            int k = 0;
            while (operation.size() > 0 && !operation.get(j).id.equals(id) && k < operation.size() + 1) {
                j = (j + 1) % operation.size();
                k++;
            }
            if (operation.size() == 0 || !operation.get(j).id.equals(id)) {
                int transactionId = parseOperation.getInt("transaction");
                String fromCircle = parseOperation.getString("from");
                String toCircle = parseOperation.getString("to");
                Date operDate = parseOperation.getDate("oper_date");
                int pageNo = parseOperation.getInt("pageno");
                float amount = (float)parseOperation.getDouble("amount");
                String description = parseOperation.getString("description");
                boolean deleted = parseOperation.getBoolean("deleted");
                operation.add(new com.gi123i.maxflow.Operation(id,fromCircle,toCircle,amount,operDate,transactionId,pageNo,true,true,parseOperation.getObjectId(),description,deleted));
            } else {
                com.gi123i.maxflow.Operation oper = operation.get(j);
                oper.amount = (float)parseOperation.getDouble("amount");
                oper.date = parseOperation.getDate("oper_date");
                oper.pageNo = parseOperation.getInt("pageno");
                oper.description = parseOperation.getString("description");
                oper.deleted = parseOperation.getBoolean("deleted");
                oper.cloudId = parseOperation.getObjectId();
                operation.set(j,oper);

            }

        }


    }

    public void syncCircleList(List<ParseObject> parseCircleList, ArrayList<Circle> circle, Properties properties) {
        int j = 0;
        for (int i = 0; i < parseCircleList.size(); i++) {
            ParseObject parseCircle = parseCircleList.get(i);
            if (parseCircle.getUpdatedAt().compareTo(properties.lastCloudUpdateCircle) > 0)
                properties.lastCloudUpdateCircle = parseCircle.getUpdatedAt();
            String id = parseCircle.getString("circle_id");
            int k = 0;
            while (circle.size() > 0 && !circle.get(j).id.equals(id) && k < circle.size() + 1) {
                j = (j + 1) % circle.size();
                k++;
            }
            if (circle.size() == 0 || !circle.get(j).id.equals(id)) {
                String name = parseCircle.getString("name");
                PointF newpoint = new PointF((float)parseCircle.getDouble("coordinate_x"),
                        (float)parseCircle.getDouble("coordinate_y"));
                float newradius = (float)parseCircle.getDouble("radius");
                boolean deleted = parseCircle.getBoolean("deleted");
                String cloudId = parseCircle.getObjectId();
                int picture = parseCircle.getInt("picture");
                int color = parseCircle.getInt("color");
                String parentId = "";
                if (parseCircle.getString("parentId") != null)
                    parentId = parseCircle.getString("parentId");
                circle.add(new Circle(id,parentId,newpoint,newradius,name,deleted,picture,color,true,true,cloudId));
            } else {
                Circle acc = circle.get(j);
                acc.name = parseCircle.getString("name");
                acc.coordinates = new PointF((float)parseCircle.getDouble("coordinate_x"),
                        (float)parseCircle.getDouble("coordinate_y"));
                acc.radius = (float)parseCircle.getDouble("radius");
                acc.deleted = parseCircle.getBoolean("deleted");
                acc.syncedWithCloud = true;
                acc.cloudId = parseCircle.getObjectId();
                acc.picture = parseCircle.getInt("picture");
                acc.color = parseCircle.getInt("color");
                if (parseCircle.getString("parentId") != null)
                    acc.parentId = parseCircle.getString("parentId");
                circle.set(j,acc);
            }

        }
    }


    public void uploadCircles(final ArrayList<Circle> circle, String filename, Properties properties) {
        if (initialized) {
            if (properties.firebaseUserEmail.equals(""))
                return;
            for (int i = 0; i < circle.size(); i++) {
                Circle myCircle = circle.get(i);
                if (!myCircle.syncedWithCloud) {
                    if (!myCircle.sentToCloud) {
                        ParseObject testCircle = new ParseObject("TestCircle");
                        testCircle.put("username", properties.firebaseUserEmail);
                        testCircle.put("filename", filename);
                        testCircle.put("circle_id", myCircle.id);
                        testCircle.put("name", myCircle.name);
                        testCircle.put("coordinate_x", myCircle.coordinates.x);
                        testCircle.put("coordinate_y", myCircle.coordinates.y);
                        testCircle.put("radius", myCircle.radius);
                        testCircle.put("picture", myCircle.picture);
                        testCircle.put("color", myCircle.color);
                        testCircle.put("deleted", myCircle.deleted);
                        testCircle.put("parentId", myCircle.parentId);
                        //testCircle.saveInBackground();
                        myCircle.sentToCloud = true;
                        myCircle.syncedWithCloud = true;
                        circle.set(i, myCircle);
                        testCircle.saveEventually();
                        Log.e("TestCircle", "Submitted one circle");
                    } else {
                        final int i1 = i;
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("TestCircle");
                        query.setLimit(10000);
                        query.whereEqualTo("username", properties.firebaseUserEmail);
                        query.whereEqualTo("filename", filename);
                        query.getInBackground(myCircle.cloudId, new GetCallback<ParseObject>() {
                            public void done(ParseObject testCircle, ParseException e) {
                                if (e == null) {
                                    Circle myCircle = circle.get(i1);
                                    testCircle.put("name", myCircle.name);
                                    testCircle.put("coordinate_x", myCircle.coordinates.x);
                                    testCircle.put("coordinate_y", myCircle.coordinates.y);
                                    testCircle.put("radius", myCircle.radius);
                                    testCircle.put("picture", myCircle.picture);
                                    testCircle.put("color", myCircle.color);
                                    testCircle.put("deleted", myCircle.deleted);
                                    testCircle.put("parentId", myCircle.parentId);
                                    //testCircle.saveInBackground();
                                    testCircle.saveEventually();
                                    Log.e("TestCircle", "Changed one circle");
                                    myCircle.syncedWithCloud = true;
                                    circle.set(i1, myCircle);
                                }
                            }
                        });
                    }


                }
            }
        }
    }
    public void uploadOperations(final ArrayList<com.gi123i.maxflow.Operation> operation, String filename, Properties properties) {
        if (initialized) {
            if (properties.firebaseUserEmail.equals(""))
                return;
            for (int i = 0; i < operation.size(); i++) { //ALL, including deleted
                com.gi123i.maxflow.Operation myOperation = operation.get(i);
                if (!myOperation.syncedWithCloud) {
                    if (!myOperation.sentToCloud) {
                        ParseObject testOperation = new ParseObject("TestOperation");
                        testOperation.put("username", properties.firebaseUserEmail);
                        testOperation.put("filename", filename);
                        testOperation.put("operation_id", myOperation.id);
                        testOperation.put("transaction", myOperation.transactionId);
                        testOperation.put("from", myOperation.fromCircle);
                        testOperation.put("to", myOperation.toCircle);
                        testOperation.put("pageno", myOperation.pageNo);
                        testOperation.put("oper_date", myOperation.date);
                        testOperation.put("amount", myOperation.amount);
                        testOperation.put("description", myOperation.description);
                        testOperation.put("deleted",myOperation.deleted);
                        //testOperation.saveInBackground();
                        testOperation.saveEventually();
                        Log.e("TestCircle", "Submitted one operation");
                        myOperation.syncedWithCloud = true;
                        myOperation.sentToCloud = true;
                        operation.set(i, myOperation);
                    } else {
                        final int i1 = i;
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("TestOperation");
                        query.setLimit(10000);
                        query.whereEqualTo("username", properties.firebaseUserEmail);
                        query.whereEqualTo("filename", filename);
                        query.getInBackground(myOperation.cloudId, new GetCallback<ParseObject>() {
                            public void done(ParseObject testOperation, ParseException e) {
                                if (e == null) {
                                    com.gi123i.maxflow.Operation myOperation = operation.get(i1);
                                    testOperation.put("amount", myOperation.amount);
                                    testOperation.put("oper_date", myOperation.date);
                                    testOperation.put("pageno", myOperation.pageNo);
                                    testOperation.put("description",myOperation.description);
                                    testOperation.put("deleted",myOperation.deleted);
                                    //testOperation.saveInBackground();
                                    testOperation.saveEventually();
                                    myOperation.syncedWithCloud = true;
                                    operation.set(i1, myOperation);
                                    Log.e("TestCircle","Changed one operation");
                                }
                            }
                        });
                    }


                }
            }
        }
    }

    public void downloadFiles(final ArrayList<String> filesInCloud, Storage storage, Properties properties) {
        if (initialized) {
            if (properties.firebaseUserEmail.equals(""))
                return;
            filesInCloud.clear();
            final Storage _storage = storage;
            final Properties _properties = properties;
            ParseQuery<ParseObject> query = ParseQuery.getQuery("TestCircle");
            query.setLimit(10000);
            query.whereEqualTo("username", properties.firebaseUserEmail);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> parseCircle, ParseException e) {
                    if (e == null) {

                        for (int i = 0; i < parseCircle.size(); i++) {
                            ParseObject _parseCircle = parseCircle.get(i);
                            String filename = _parseCircle.getString("filename");
                            if (!_parseCircle.getBoolean("deleted"))
                                if (!filesInCloud.contains(filename)) {
                                    Log.e("Found file", filename);
                                    filesInCloud.add(filename);
                                }
                        }
                        _storage.syncFiles(filesInCloud, _properties);
                    } else {
                        Log.d("TestCircle", "Error: " + e.getMessage());
                    }
                }
            });
        }
    }
    */
}