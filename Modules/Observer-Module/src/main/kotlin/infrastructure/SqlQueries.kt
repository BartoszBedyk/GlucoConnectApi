package infrastructure

internal object SqlQueries {

    const val CREATE_OBSERVERS_TABLE = """CREATE TABLE IF NOT EXISTS glucoconnectapi.observers (
    id CHAR(36) PRIMARY KEY,
    observer_id CHAR(36) NOT NULL REFERENCES glucoconnectapi.users(id) ON DELETE CASCADE,
    observed_id CHAR(36) NOT NULL REFERENCES glucoconnectapi.users(id) ON DELETE CASCADE,
    is_accepted BOOLEAN DEFAULT FALSE
);
"""
    const val CREATE_OBSERVATION =
        """INSERT INTO glucoconnectapi.observers(id, observer_id, observed_id) VALUES (?,?,?) """

    const val GET_OBSERVED_ACCEPTED_BY_OBSERVER =
        """ SELECT * FROM glucoconnectapi.observers WHERE observer_id = ? AND is_accepted = ?"""

    const val GET_OBSERVED_UNACCEPTED_BY_OBSERVER =
        """ SELECT * FROM glucoconnectapi.observers WHERE observer_id = ? AND is_accepted = ?"""

    const val GET_ACCEPTED_OBSERVER_BY_OBSERVED_ID =
        """ SELECT * FROM glucoconnectapi.observers WHERE observed_id = ? AND is_accepted = ?"""

    const val GET_UNACCEPTED_OBSERVER_BY_OBSERVED_ID =
        """ SELECT * FROM glucoconnectapi.observers WHERE observed_id = ? AND is_accepted = ?"""

    const val ACCEPT_OBSERVATION = """UPDATE glucoconnectapi.observers
                   SET is_accepted = ?
                   WHERE observer_id = ? AND observed_id = ?"""

    const val UN_ACCEPT_OBSERVATION = """UPDATE glucoconnectapi.observers
                   SET is_accepted = ?
                   WHERE observer_id = ? AND observed_id = ?"""


}