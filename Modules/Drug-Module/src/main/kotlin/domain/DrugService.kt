package domain

import data.DrugRepository
import model.CreateDrugRequest

class DrugService(private val repository: DrugRepository) {

    fun getDrugById(id: Long) = repository.findDrugById(id)
    fun createDrug(form: CreateDrugRequest) = repository.createDrug(form)
}
