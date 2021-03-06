package io.rently.searchservice.services;

import io.rently.searchservice.apis.TomTom;
import io.rently.searchservice.dtos.Listing;
import io.rently.searchservice.exceptions.Errors;
import io.rently.searchservice.interfaces.ListingsRepository;
import io.rently.searchservice.utils.Broadcaster;
import io.rently.searchservice.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SearchService {

    @Autowired
    private ListingsRepository repository;

    @Autowired
    private TomTom tomtomApi;

    public Listing queryById(String id) {
        Optional<Listing> listing = repository.findById(id);
        if (listing.isPresent()) {
            return listing.get();
        } else {
            throw Errors.LISTING_NOT_FOUND;
        }
    }

    public List<Listing> queryRandomly(Integer count) {
        Broadcaster.info("Fetching listings randomly. Pagination: count = " +  count);
        return repository.queryAny(count);
    }

    public Page<Listing> queryListings(String query, Integer count, Integer offset) {
        Pageable pagination = PageRequest.of(offset, count);
        if (query != null) {
            String regexQuery = Utils.getKeywordsFromQuery(query);
            Broadcaster.info("Fetching listings by query. Pagination: count = " + pagination.getPageSize() + ", page = " + pagination.getPageNumber());
            Broadcaster.info("Parameters: query = " + regexQuery);
            return repository.query(regexQuery, pagination);
        }
        Broadcaster.info("Fetching listings in order. Pagination: count = " + pagination.getPageSize() + ", page = " + pagination.getPageNumber());
        return repository.findAll(pagination);
    }

    public Page<Listing> queryListingsNearbyGeo(String query, Double lat, Double lon, Integer range, Integer count, Integer offset) {
        Pageable pagination = PageRequest.of(offset, count);
        if (query != null) {
            String regexQuery = Utils.getKeywordsFromQuery(query);
            Broadcaster.info("Fetching listings by query and geocode. Pagination: count = " + pagination.getPageSize() + ", offset = " + pagination.getPageNumber());
            Broadcaster.info("Parameters: query = " + regexQuery + " lat = " + lat + ", lon = " + lon);
            return repository.queryNearbyGeoCode(Utils.getKeywordsFromQuery(query), lat, lon, range, pagination);
        }
        Broadcaster.info("Fetching listings nearby geocode. Pagination: count = " + pagination.getPageSize() + ", offset = " + pagination.getPageNumber());
        Broadcaster.info("Parameters: lat = " + lat + ", lon = " + lon);
        return repository.queryAnyNearbyGeoCode(lat, lon, range, pagination);
    }

    public Page<Listing> queryListingsNearbyAddress(String query, Integer range, Integer count, Integer offset, String address) {
        Pageable pagination = PageRequest.of(offset, count);
        Pair<Double, Double> geoCords;
        try {
            geoCords = tomtomApi.getGeoFromAddress(address);
        } catch (Exception ex) {
            throw Errors.NO_ADDRESS_FOUND;
        }
        if (query != null) {
            Broadcaster.info("Fetching listings by query and nearby location. Pagination: count = " + pagination.getPageSize() + ", offset = " + pagination.getPageNumber());
            Broadcaster.info("Parameters: query = " + Utils.getKeywordsFromQuery(query) + ", address = " + String.join(" ", address)  + ", range = " + range);
            return repository.queryNearbyGeoCode(Utils.getKeywordsFromQuery(query), geoCords.getFirst(), geoCords.getSecond(), range, pagination);
        }
        Broadcaster.info("Fetching listings nearby location. Pagination: count = " + pagination.getPageSize() + ", offset = " + pagination.getPageNumber());
        Broadcaster.info("Parameters: address = " + String.join(" ", address) + ", range = " + range);
        return repository.queryAnyNearbyGeoCode(geoCords.getFirst(), geoCords.getSecond(), range, pagination);
    }

    public Page<Listing> queryListingsAtAddress(String query, String country, String city, String zip, Integer count, Integer offset) {
        Pageable pagination = PageRequest.of(offset, count);
        if (country == null && city == null) {
            throw Errors.NO_ADDRESS_PARAMS;
        }
        if (query != null) {
            Broadcaster.info("Fetching listings by query and location. Pagination: count = " + pagination.getPageSize() + ", offset = " + pagination.getPageNumber());
            Broadcaster.info("Parameters: query = " + Utils.getKeywordsFromQuery(query) + ", country = " + country + ", city = " + city + ", zip = " + zip);
            return repository.queryAtAddress(Utils.getKeywordsFromQuery(query), country, city, zip, pagination);
        }
        Broadcaster.info("Fetching listings by location. Pagination: count = " + pagination.getPageSize() + ", offset = " + pagination.getPageNumber());
        Broadcaster.info("Parameters: country = " + country + ", city = " + city + ", zip = " + zip);
        return repository.queryAnyAtAddress(country, city, zip, pagination);
    }
}
