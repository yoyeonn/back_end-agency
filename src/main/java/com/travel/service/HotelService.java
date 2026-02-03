package com.travel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.dto.HotelDTO;
import com.travel.entity.*;
import com.travel.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CloudinaryService cloudinaryService;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    // ==========================
    // GET
    // ==========================
    public List<HotelDTO> getAllHotels() {
        return hotelRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public HotelDTO getHotelById(Long id) {
        return hotelRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));
    }

    public List<HotelDTO> search(String keyword) {
        List<Hotel> hotels;
        if (keyword == null || keyword.isBlank()) {
            hotels = hotelRepository.findAll();
        } else {
            hotels = hotelRepository.searchHotels(keyword);
        }
        return hotels.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<String> getSuggestions(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();
        String lowerKeyword = keyword.toLowerCase();

        return hotelRepository.findAll().stream()
                .filter(h ->
                        (h.getName() != null && h.getName().toLowerCase().contains(lowerKeyword)) ||
                        (h.getCity() != null && h.getCity().toLowerCase().contains(lowerKeyword)) ||
                        (h.getCountry() != null && h.getCountry().toLowerCase().contains(lowerKeyword))
                )
                .map(h -> h.getName() + ", " + h.getCity() + ", " + h.getCountry())
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    // ==========================
    // CREATE
    // ==========================
    public HotelDTO createHotel(HotelDTO dto) {
        Hotel hotel = new Hotel();

        hotel.setName(dto.getName());
        hotel.setDescription(dto.getDescription());
        hotel.setmap(dto.getmap());
        hotel.setCity(dto.getCity());
        hotel.setCountry(dto.getCountry());
        hotel.setDays(dto.getDays());
        hotel.setStars(dto.getStars());
        hotel.setAbout(dto.getAbout());

        hotel.setCancellationPolicy(dto.getCancellationPolicy());
        hotel.setAvailableDates(dto.getAvailableDates());
        hotel.setBestTimeToVisit(dto.getBestTimeToVisit());

        // store lists as JSON
        hotel.setImages(toJsonString(dto.getImages()));
        hotel.setHighlights(toJsonString(dto.getHighlights()));
        hotel.setTravelTips(toJsonString(dto.getTravelTips()));
        hotel.setSuitedFor(toJsonString(dto.getSuitedFor()));
        hotel.setIncludes(toJsonString(dto.getIncludes()));
        hotel.setExcludes(toJsonString(dto.getExcludes()));

        // rooms
        hotel.setRooms(new ArrayList<>());
        if (dto.getRooms() != null) {
            dto.getRooms().forEach(r -> {
                Room room = new Room();
                room.setName(r.getName());
                room.setImage(r.getImage()); // can be url or empty
                room.setDescription(r.getDescription());
                room.setCapacity(r.getCapacity());
                room.setPrice(r.getPrice());
                room.setHotel(hotel);
                hotel.getRooms().add(room);
            });
        }

        // faq
        hotel.setFaq(new ArrayList<>());
        if (dto.getFaq() != null) {
            dto.getFaq().forEach(f -> {
                Faq faq = new Faq();
                faq.setQuestion(f.getQuestion());
                faq.setAnswer(f.getAnswer());
                faq.setOpen(f.isOpen());
                faq.setHotel(hotel);
                hotel.getFaq().add(faq);
            });
        }

        // nearby
        hotel.setNearby(new ArrayList<>());
        if (dto.getNearby() != null) {
            dto.getNearby().forEach(n -> {
                Nearby nb = new Nearby();
                nb.setName(n.getName());
                nb.setDistance(n.getDistance());
                nb.setHotel(hotel);
                hotel.getNearby().add(nb);
            });
        }

        // programme
        hotel.setProgramme(new ArrayList<>());
        if (dto.getProgramme() != null) {
            dto.getProgramme().forEach(p -> {
                Programme prog = new Programme();
                prog.setDay(p.getDay());
                prog.setActivity(p.getActivity());
                prog.setHotel(hotel);
                hotel.getProgramme().add(prog);
            });
        }

        Hotel saved = hotelRepository.save(hotel);
        return convertToDTO(saved);
    }

    // ==========================
    // UPDATE (FIXED ROOMS MERGE)
    // ==========================
    public HotelDTO updateHotel(Long id, HotelDTO dto) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));

        // basic fields
        hotel.setName(dto.getName());
        hotel.setDescription(dto.getDescription());
        hotel.setmap(dto.getmap());
        hotel.setCity(dto.getCity());
        hotel.setCountry(dto.getCountry());
        hotel.setDays(dto.getDays());
        hotel.setStars(dto.getStars());
        hotel.setAbout(dto.getAbout());

        hotel.setCancellationPolicy(dto.getCancellationPolicy());
        hotel.setAvailableDates(dto.getAvailableDates());
        hotel.setBestTimeToVisit(dto.getBestTimeToVisit());

        // ✅ update JSON fields only if client sent them (prevents wiping)
        if (dto.getImages() != null) hotel.setImages(toJsonString(dto.getImages()));
        if (dto.getHighlights() != null) hotel.setHighlights(toJsonString(dto.getHighlights()));
        if (dto.getTravelTips() != null) hotel.setTravelTips(toJsonString(dto.getTravelTips()));
        if (dto.getSuitedFor() != null) hotel.setSuitedFor(toJsonString(dto.getSuitedFor()));
        if (dto.getIncludes() != null) hotel.setIncludes(toJsonString(dto.getIncludes()));
        if (dto.getExcludes() != null) hotel.setExcludes(toJsonString(dto.getExcludes()));

        // ✅ ROOMS: MERGE BY ID (keeps images unless new provided)
        if (hotel.getRooms() == null) hotel.setRooms(new ArrayList<>());

        Map<Long, Room> existingRooms = hotel.getRooms().stream()
                .filter(r -> r.getId() != null)
                .collect(Collectors.toMap(Room::getId, r -> r));

        List<Room> mergedRooms = new ArrayList<>();

        if (dto.getRooms() != null) {
            for (HotelDTO.RoomDTO rDto : dto.getRooms()) {
                Room room;

                // existing
                if (rDto.getId() != null && existingRooms.containsKey(rDto.getId())) {
                    room = existingRooms.get(rDto.getId());

                    room.setName(rDto.getName());
                    room.setDescription(rDto.getDescription());
                    room.setCapacity(rDto.getCapacity());
                    room.setPrice(rDto.getPrice());

                    // ✅ keep old image if dto image is empty/null
                    if (rDto.getImage() != null && !rDto.getImage().isBlank()) {
                        room.setImage(rDto.getImage());
                    }

                } else {
                    // new
                    room = new Room();
                    room.setName(rDto.getName());
                    room.setDescription(rDto.getDescription());
                    room.setCapacity(rDto.getCapacity());
                    room.setPrice(rDto.getPrice());
                    room.setImage(rDto.getImage()); // can be empty initially
                }

                room.setHotel(hotel);
                mergedRooms.add(room);
            }
        }

        // replace list => also removes deleted rooms
        hotel.getRooms().clear();
        hotel.getRooms().addAll(mergedRooms);

        // FAQ
        if (hotel.getFaq() == null) hotel.setFaq(new ArrayList<>());
        hotel.getFaq().clear();
        if (dto.getFaq() != null) {
            dto.getFaq().forEach(f -> {
                Faq faq = new Faq();
                faq.setQuestion(f.getQuestion());
                faq.setAnswer(f.getAnswer());
                faq.setOpen(f.isOpen());
                faq.setHotel(hotel);
                hotel.getFaq().add(faq);
            });
        }

        // Nearby
        if (hotel.getNearby() == null) hotel.setNearby(new ArrayList<>());
        hotel.getNearby().clear();
        if (dto.getNearby() != null) {
            dto.getNearby().forEach(n -> {
                Nearby nb = new Nearby();
                nb.setName(n.getName());
                nb.setDistance(n.getDistance());
                nb.setHotel(hotel);
                hotel.getNearby().add(nb);
            });
        }

        // Programme
        if (hotel.getProgramme() == null) hotel.setProgramme(new ArrayList<>());
        hotel.getProgramme().clear();
        if (dto.getProgramme() != null) {
            dto.getProgramme().forEach(p -> {
                Programme prog = new Programme();
                prog.setDay(p.getDay());
                prog.setActivity(p.getActivity());
                prog.setHotel(hotel);
                hotel.getProgramme().add(prog);
            });
        }

        Hotel saved = hotelRepository.save(hotel);
        return convertToDTO(saved);
    }

    // ==========================
    // DELETE HOTEL
    // ==========================
    public void deleteHotel(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));
        hotelRepository.delete(hotel);
    }

    // ==========================
    // HOTEL IMAGES (MULTI UPLOAD + DELETE BY INDEX)
    // ==========================
    public HotelDTO uploadHotelImages(Long id, List<MultipartFile> files) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));

        List<String> current = parseJsonList(hotel.getImages());
        if (current == null) current = new ArrayList<>();

        for (MultipartFile f : files) {
            String url = cloudinaryService.uploadImage(f, "hotels");
            current.add(url);
        }

        hotel.setImages(toJsonString(current));
        Hotel saved = hotelRepository.save(hotel);
        return convertToDTO(saved);
    }

    public HotelDTO deleteHotelImage(Long id, int index) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));

        List<String> current = parseJsonList(hotel.getImages());
        if (current == null) current = new ArrayList<>();

        if (index < 0 || index >= current.size()) {
            throw new RuntimeException("Invalid image index");
        }

        String url = current.remove(index);

        if (url != null && url.startsWith("http")) {
            cloudinaryService.deleteByUrl(url);
        }

        hotel.setImages(toJsonString(current));
        Hotel saved = hotelRepository.save(hotel);
        return convertToDTO(saved);
    }

    // ==========================
    // ✅ ROOM IMAGE (ONLY 1) — upload replaces / delete sets null
    // ==========================
    public HotelDTO uploadRoomImage(Long hotelId, Long roomId, MultipartFile file) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        Room room = hotel.getRooms().stream()
                .filter(r -> r.getId() != null && r.getId().equals(roomId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        // optional: delete old image from cloudinary
        if (room.getImage() != null && room.getImage().startsWith("http")) {
            cloudinaryService.deleteByUrl(room.getImage());
        }

        String url = cloudinaryService.uploadImage(file, "rooms");
        room.setImage(url); // ✅ replace old
        Hotel saved = hotelRepository.save(hotel);
        return convertToDTO(saved);
    }

    public HotelDTO deleteRoomImage(Long hotelId, Long roomId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + hotelId));

        Room room = hotel.getRooms().stream()
                .filter(r -> r.getId() != null && r.getId().equals(roomId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        if (room.getImage() != null && room.getImage().startsWith("http")) {
            cloudinaryService.deleteByUrl(room.getImage());
        }

        room.setImage(null);
        Hotel saved = hotelRepository.save(hotel);
        return convertToDTO(saved);
    }

    // ==========================
    // JSON helpers
    // ==========================
    private String toJsonString(List<String> list) {
        try {
            if (list == null) return "[]";
            List<String> clean = list.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.toList());
            return objectMapper.writeValueAsString(clean);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> parseJsonList(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            List<String> list = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            if (list == null) return List.of();
            return list.stream().filter(s -> s != null && !s.isBlank()).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    // ==========================
    // DTO mapping
    // ==========================
    private HotelDTO convertToDTO(Hotel hotel) {
        HotelDTO dto = new HotelDTO();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setDescription(hotel.getDescription());
        dto.setmap(hotel.getmap());
        dto.setCity(hotel.getCity());
        dto.setCountry(hotel.getCountry());
        dto.setDays(hotel.getDays());
        dto.setStars(hotel.getStars());
        dto.setAbout(hotel.getAbout());
        dto.setImages(parseJsonList(hotel.getImages()));
        dto.setHighlights(parseJsonList(hotel.getHighlights()));
        dto.setCancellationPolicy(hotel.getCancellationPolicy());
        dto.setAvailableDates(hotel.getAvailableDates());
        dto.setBestTimeToVisit(hotel.getBestTimeToVisit());
        dto.setTravelTips(parseJsonList(hotel.getTravelTips()));
        dto.setSuitedFor(parseJsonList(hotel.getSuitedFor()));
        dto.setIncludes(parseJsonList(hotel.getIncludes()));
        dto.setExcludes(parseJsonList(hotel.getExcludes()));

        dto.setRooms(
                hotel.getRooms() == null ? List.of() :
                        hotel.getRooms().stream().map(r -> {
                            HotelDTO.RoomDTO room = new HotelDTO.RoomDTO();
                            room.setId(r.getId());
                            room.setName(r.getName());
                            room.setImage(r.getImage());
                            room.setDescription(r.getDescription());
                            room.setCapacity(r.getCapacity());
                            room.setPrice(r.getPrice());
                            return room;
                        }).collect(Collectors.toList())
        );

        dto.setNearby(
                hotel.getNearby() == null ? List.of() :
                        hotel.getNearby().stream().map(n -> {
                            HotelDTO.NearbyDTO nearby = new HotelDTO.NearbyDTO();
                            nearby.setName(n.getName());
                            nearby.setDistance(n.getDistance());
                            return nearby;
                        }).collect(Collectors.toList())
        );

        dto.setFaq(
                hotel.getFaq() == null ? List.of() :
                        hotel.getFaq().stream().map(f -> {
                            HotelDTO.FaqDTO faq = new HotelDTO.FaqDTO();
                            faq.setQuestion(f.getQuestion());
                            faq.setAnswer(f.getAnswer());
                            faq.setOpen(f.isOpen());
                            return faq;
                        }).collect(Collectors.toList())
        );

        dto.setProgramme(
                hotel.getProgramme() == null ? List.of() :
                        hotel.getProgramme().stream().map(p -> {
                            HotelDTO.ProgrammeDTO prog = new HotelDTO.ProgrammeDTO();
                            prog.setDay(p.getDay());
                            prog.setActivity(p.getActivity());
                            return prog;
                        }).collect(Collectors.toList())
        );

        return dto;
    }
}
