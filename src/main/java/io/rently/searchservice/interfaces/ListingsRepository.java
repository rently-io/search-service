package io.rently.searchservice.interfaces;

import io.rently.searchservice.dtos.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ListingsRepository extends MongoRepository<Listing, String> {

    @Aggregation("{ $sample: { size: ?0 } }")
    List<Listing> queryAny(Integer size);

    @Query("{ $or: [ {'name': {$regex: ?0, $options: 'i'} }, {'desc': {$regex: ?0, $options: 'i'} } ] }")
    Page<Listing> query(String query, Pageable pageable);

    @Query("{ 'address.location': { $near: {$maxDistance: ?2, $geometry: {type: 'Point', coordinates: [?1, ?0]} } } }")
    Page<Listing> queryAnyNearbyGeoCode(Double lat, Double lon, Integer range, Pageable pageable);

    @Query("{ $and: [" +
//            "  { $or: [ {'name': {$regex: 'bbq', $options: 'i'} }, {'desc': {$regex: 'bbq', $options: 'i'} }, { 'address.formattedAddress': { $regex: '', $options: 'i' } } ] }," +
            "  { $or: [ {'name': {$regex: 'bbq', $options: 'i'} }, {'desc': {$regex: 'bbq', $options: 'i'} } ] }," +
            "  { 'address.location': { $near: { $maxDistance: 10000, $geometry: {type: 'Point', coordinates: [7.0897, 49.0902]} } } }," +
            "] }")
    Page<Listing> queryNearbyGeoCode(String query, Double lat, Double lon, Integer range, Pageable pageable);

    @Query("{ $or: [ {'address.country': /?1/}, {'address.city': /?2/}, {'address.zip': /?3/} ] }")
    Page<Listing> queryAnyAtAddress(String country, String city, String zip, Pageable pageable);

    @Query("{ $and: [" +
            "{ $or: [ {'name': {$regex: ?0, $options: 'i'} }, {'desc': {$regex: ?0, $options: 'i'} } ] }" +
            "{ $or: [ {'address.country': ?1}, {'address.city': ?2}, {'address.zip': ?3} ] }" +
            "] }")
    Page<Listing> queryAtAddress(String query, String country, String city, String zip, Pageable pageable);

    Page<Listing> findByAddressLocationNearAndNameIgnoreCaseRegex(Point point, Distance distance, String name, Pageable pageable);

    Page<Listing> findByNameIgnoreCaseRegex(String name, Pageable pageable);
}
