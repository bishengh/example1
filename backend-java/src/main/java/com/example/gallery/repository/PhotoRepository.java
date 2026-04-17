package com.example.gallery.repository;

import com.example.gallery.model.Album;
import com.example.gallery.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByAlbum(Album album);
}
