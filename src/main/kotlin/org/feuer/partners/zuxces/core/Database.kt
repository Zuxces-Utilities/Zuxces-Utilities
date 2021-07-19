package org.feuer.partners.zuxces.core

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.litote.kmongo.findOne
import java.lang.Exception

class Database {
    private val config = ConfigurationManager()
    private val client = MongoClient(config["mongo_ip"], config["mongo_port"].toInt())
    private val db: MongoDatabase = client.getDatabase("Zuxces")
    fun getCollection(collection: String): MongoCollection<Document>? {
        val pojoCodecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )
        return try {
            db.getCollection(collection).withCodecRegistry(pojoCodecRegistry)
        } catch (e: Exception) {
            null
        }
    }
    fun findDocument(collection: MongoCollection<Document>, key: String, value: String): Document? {
        val query = BasicDBObject()
        query[key] = value
        return collection.findOne(query)
    }

}