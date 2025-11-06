package infrastructure

internal object SqlQueries {

    const val CREATE_ACTIVITY_TABLE = """CREATE TABLE IF NOT EXISTS activities (
        id CHAR(36) NOT NULL PRIMARY KEY,
        activity_type CHAR(50) NOT NULL,
        creation_date TIMESTAMP,
        created_by_id CHAR(36) NOT NULL);
           """

    const val CREATE_ACTIVITY = """INSERT INTO activities (id, activity_type, creation_date, created_by_id)
            VALUES (?, ?, ?, ?)
        """
    const val GET_ACTIVITY_BY_ID = "SELECT id, activity_type, creation_date," +
        " created_by_id FROM activities WHERE id =?"

    const val GET_ACTIVITY_BY_TYPE = "SELECT id, activity_type, creation_date," +
        " created_by_id FROM activities WHERE activity_type = ?"

    const val GET_ACTIVITY_BY_USER_ID = "SELECT id, activity_type, creation_date," +
        " created_by_id FROM activities WHERE created_by_id = ?"
}
