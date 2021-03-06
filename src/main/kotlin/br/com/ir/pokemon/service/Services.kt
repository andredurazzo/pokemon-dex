package br.com.ir.pokemon.service

import br.com.ir.pokemon.entities.Trainer
import br.com.ir.pokemon.exceptions.NotFoundException
import br.com.ir.pokemon.model.PokemonRequest
import br.com.ir.pokemon.model.PokemonResponse
import br.com.ir.pokemon.model.TrainerRequest
import br.com.ir.pokemon.repository.PokemonDetailsRepository
import br.com.ir.pokemon.repository.PokemonRepository
import br.com.ir.pokemon.repository.TrainerRepository
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class TrainerService( @Autowired private var repository: TrainerRepository){
    companion object {
        private val log = KotlinLogging.logger { }
    }


    fun addTrainer(request: TrainerRequest): Mono<Trainer> {
        log.info { "prepare to insert Trainer[{ $request }]"  }

        checkNotNull(request.birthdate)
        checkNotNull(request.nickName)
        return repository.save(request.toEntity())
    }

    fun findAll():Flux<Trainer> {
        return repository.findAll()
    }


    fun findById(id:String):Mono<Trainer> {
        log.info { "find trainer by id[{ $id }]"}
        val intId:Mono<Int> = Mono.justOrEmpty(id)
            .map { i -> i.toInt() }

        return repository.findById(intId)
            .switchIfEmpty(Mono.error(NotFoundException))
    }

    fun updateTrainer(request: TrainerRequest): Mono<Trainer> {
        log.info { "updated trainer [{$request}]"}
        request.update_at = LocalDateTime.now()
        return addTrainer(request)

    }

    fun deleteTrainer(id:String): Mono<Void>{
        log.info { "delete trainer by id[{ $id }]"}
        return  findById(id)
            .flatMap { trainer -> repository.delete(trainer) }
            .then(Mono.empty())
    }

}

@Service
class PokemonService(@Autowired private var repository: PokemonRepository, @Autowired private var  detailsRepository:PokemonDetailsRepository  ){

    fun findAll():Flux<PokemonResponse>{

        return repository.findAll().map{
            PokemonResponse.Builder().pokemon(it).baseStatus(detailsRepository.findById(it).block()).build()
        }
    }

    fun addPokemon(request: PokemonRequest): Mono<PokemonResponse>{


        repository.save(request.pokemon!!)
        return Mono.empty()
    }

}