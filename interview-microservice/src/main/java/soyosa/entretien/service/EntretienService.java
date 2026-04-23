package soyosa.entretien.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import soyosa.entretien.Domain.Entretien;
import soyosa.entretien.Domain.Status;
import soyosa.entretien.repository.EntretienRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class EntretienService {

    private final EntretienRepository entretienRepository;
    private final CloudinaryService cloudinaryService;

    public EntretienService(EntretienRepository repository, CloudinaryService cloudinaryService) {
        this.entretienRepository = repository;
        this.cloudinaryService = cloudinaryService;
    }

    public Entretien createEntretien(MultipartFile file, LocalDate date, String user) {
        String cvUrl = cloudinaryService.uploadFile(file);

        Entretien e = new Entretien();
        e.setUser(user);
        e.setDate(date);
        e.setStatus(Status.PENDING);
        e.setCvUrl(cvUrl);

       return entretienRepository.save(e);

    }


    public List<Entretien> getAllEntretiens() {
        return entretienRepository.findAll();
    }


    public Optional<Entretien> getEntretienById(Long id) {
        return entretienRepository.findById(id);
    }


    public Entretien updateEntretien(Long id,Entretien entretien) {
        Entretien exist = entretienRepository.findById(id).get();
        exist.setAdministrator(entretien.getAdministrator());
        exist.setTime(entretien.getTime());
        exist.setStatus(Status.SCHEDULED);
        return entretienRepository.save(exist);
    }


    public void deleteEntretien(Long id) {
        entretienRepository.deleteById(id);
    }

    public Entretien getEntretienByUsername(String user) {
        return entretienRepository.findByUser(user);
    }

    public Entretien passJobInterview(Long id) {
        Entretien entre = entretienRepository.findById(id).get();
        entre.setStatus(Status.PASSED);
        return entretienRepository.save(entre);
    }

    public Entretien failJobInterview(Long id) {
        Entretien entre = entretienRepository.findById(id).get();
        entre.setStatus(Status.FAILED);
        return entretienRepository.save(entre);
    }


}
